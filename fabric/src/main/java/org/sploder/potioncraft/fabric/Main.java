package org.sploder.potioncraft.fabric;

import net.fabricmc.api.ModInitializer;

import org.sploder.potioncraft.common.Common;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        Common.initialize(new FabricLoaderImpl());
    }
}

