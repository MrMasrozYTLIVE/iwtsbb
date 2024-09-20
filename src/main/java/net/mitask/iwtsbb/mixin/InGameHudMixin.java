package net.mitask.iwtsbb.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.JumpingMount;
import net.minecraft.util.Identifier;
import net.mitask.iwtsbb.Iwtsbb;
import net.mitask.iwtsbb.config.ConfigWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private static Identifier JUMP_BAR_BACKGROUND_TEXTURE;
    @Shadow @Final private static Identifier JUMP_BAR_COOLDOWN_TEXTURE;
    @Shadow @Final private static Identifier JUMP_BAR_PROGRESS_TEXTURE;
    @Shadow @Final private static Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE;
    @Shadow @Final private static Identifier EXPERIENCE_BAR_PROGRESS_TEXTURE;

    @Unique ConfigWrapper.BarChoice XPBar = ConfigWrapper.BarChoice.XP;
    @Unique ConfigWrapper.BarChoice JumpBar = ConfigWrapper.BarChoice.JUMP;

    @Redirect(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderExperienceBar(Lnet/minecraft/client/gui/DrawContext;I)V"))
    private void iwtsbb_renderXPBar(InGameHud instance, DrawContext context, int x) {
        renderXP(false, context, x);
        if(Iwtsbb.CONFIG.jumpBarWhenNoMount()) renderJump(null, context, x);
    }

    @Redirect(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountJumpBar(Lnet/minecraft/entity/JumpingMount;Lnet/minecraft/client/gui/DrawContext;I)V"))
    private void iwtsbb_renderJumpBar(InGameHud instance, JumpingMount mount, DrawContext context, int x) {
        renderXP(mount != null, context, x);
        renderXPLevel(instance, context);
        renderJump(mount, context, x);
    }

    @Unique
    private void renderXPLevel(InGameHud instance, DrawContext context) {
        if(isBarReallyDisabled(XPBar)) return;
        if(this.client == null || this.client.player == null) throw new IllegalStateException("[IWTSBB] Client or Player is null during rendering XP bar!");
        int xpLevel = this.client.player.experienceLevel;
        if (xpLevel <= 0) return;

        String xpStr = "" + xpLevel;
        int x = (context.getScaledWindowWidth() - instance.getTextRenderer().getWidth(xpStr)) / 2;
        int y = context.getScaledWindowHeight() - 35;
        context.drawText(instance.getTextRenderer(), xpStr, x + 1, y, 0, false);
        context.drawText(instance.getTextRenderer(), xpStr, x - 1, y, 0, false);
        context.drawText(instance.getTextRenderer(), xpStr, x, y + 1, 0, false);
        context.drawText(instance.getTextRenderer(), xpStr, x, y - 1, 0, false);
        context.drawText(instance.getTextRenderer(), xpStr, x, y, 8453920, false);
    }

    @Unique
    private void renderXP(boolean onMount, DrawContext context, int x) {
        if(isBarDisabled(XPBar, onMount)) return;
        if(this.client == null || this.client.player == null) throw new IllegalStateException("[IWTSBB] Client or Player is null during rendering XP bar!");
        if(this.client.player.getNextLevelExperience() <= 0) return;

        int barWidth = getWidth(XPBar, onMount);
        int xpProgress = (int) (this.client.player.experienceProgress * barWidth);
        int y = context.getScaledWindowHeight() - 29;
        x += getXModifier(XPBar, onMount);

        RenderSystem.enableBlend();
        context.drawGuiTexture(EXPERIENCE_BAR_BACKGROUND_TEXTURE, x, y, barWidth, 5);
        if (xpProgress > 0) {
            context.drawGuiTexture(EXPERIENCE_BAR_PROGRESS_TEXTURE, barWidth, 5, 0, 0, x, y, xpProgress, 5);
        }
        RenderSystem.disableBlend();
    }

    @Unique
    private void renderJump(JumpingMount mount, DrawContext context, int x) {
        if(isBarDisabled(JumpBar)) return;
        if(this.client == null || this.client.player == null) throw new IllegalStateException("[IWTSBB] Client or Player is null during rendering XP bar!");

        int barWidth = getWidth(JumpBar, mount != null);
        int jumpStrength = (int) (this.client.player.getMountJumpStrength() * barWidth);
        int y = context.getScaledWindowHeight() - 29;
        x += getXModifier(JumpBar, mount != null);

        RenderSystem.enableBlend();
        context.drawGuiTexture(JUMP_BAR_BACKGROUND_TEXTURE, x, y, barWidth, 5);
        if (mount != null && mount.getJumpCooldown() > 0) {
            context.drawGuiTexture(JUMP_BAR_COOLDOWN_TEXTURE, x, y, barWidth, 5);
        } else if (jumpStrength > 0) {
            context.drawGuiTexture(JUMP_BAR_PROGRESS_TEXTURE, barWidth, 5, 0, 0, x, y, jumpStrength, 5);
        }
        RenderSystem.disableBlend();
    }

    @Unique
    private boolean isBarDisabled(ConfigWrapper.BarChoice currentBar, boolean onMount) {
        if(!Iwtsbb.CONFIG.jumpBarWhenNoMount() && Iwtsbb.CONFIG.leftBar() == Iwtsbb.CONFIG.rightBar() && Iwtsbb.CONFIG.rightBar() == JumpBar && !onMount) return false;
        return isBarReallyDisabled(currentBar);
    }
    @Unique
    private boolean isBarDisabled(ConfigWrapper.BarChoice currentBar) {
        if(!Iwtsbb.CONFIG.jumpBarWhenNoMount() && Iwtsbb.CONFIG.leftBar() == Iwtsbb.CONFIG.rightBar() && Iwtsbb.CONFIG.rightBar() == JumpBar) return false;
        return isBarReallyDisabled(currentBar);
    }
    @Unique
    private boolean isBarReallyDisabled(ConfigWrapper.BarChoice currentBar) {
        return Iwtsbb.CONFIG.leftBar() != currentBar && Iwtsbb.CONFIG.rightBar() != currentBar;
    }

    @Unique
    private int getWidth(ConfigWrapper.BarChoice currentBar, boolean onMount) {
        if(!onMount && currentBar == XPBar && !Iwtsbb.CONFIG.jumpBarWhenNoMount()) return 182;
        if(Iwtsbb.CONFIG.leftBar() == Iwtsbb.CONFIG.rightBar()) return 182;

        return 86; // 182 / 2 = 96 - 10 = 86
    }

    @Unique
    private int getXModifier(ConfigWrapper.BarChoice currentBar, boolean onMount) {
        if(Iwtsbb.CONFIG.leftBar() == currentBar) return 0;
        if(currentBar == XPBar && !onMount && !Iwtsbb.CONFIG.jumpBarWhenNoMount()) return 0;
        return 96; // 182 - 86 = 96
    }
}
