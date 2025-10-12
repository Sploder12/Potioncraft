package net.sploder12.potioncraft.fabric;

import net.fabricmc.api.ModInitializer;

import net.sploder12.potioncraft.common.Common;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        Common.initialize(new FabricLoaderImpl());
    }
}

