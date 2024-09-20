package net.mitask.iwtsbb.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "iwtsbb")
@Config(name = "iwtsbb", wrapperName = "IwtsbbConfig")
@SuppressWarnings("unused")
public class ConfigWrapper {
    public BarChoice leftBar = BarChoice.XP;
    public BarChoice rightBar = BarChoice.JUMP;
    public boolean jumpBarWhenNoMount = false;

    public enum BarChoice {
        XP,
        JUMP
    }
}
