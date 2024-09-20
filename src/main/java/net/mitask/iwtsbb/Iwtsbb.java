package net.mitask.iwtsbb;

import net.fabricmc.api.ClientModInitializer;
import net.mitask.iwtsbb.config.IwtsbbConfig;

public class Iwtsbb implements ClientModInitializer {
    public static final IwtsbbConfig CONFIG = IwtsbbConfig.createAndLoad();

    @Override
    public void onInitializeClient() {
    }
}
