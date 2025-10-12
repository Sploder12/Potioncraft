package net.sploder12.potioncraft.common;

import net.sploder12.potioncraft.common.config.Config;
import net.sploder12.potioncraft.common.util.FluidHelper;
import net.sploder12.potioncraft.fabric.meta.FabricMetaMixing;

public abstract class Common {
    public static Loader instance = null;

    public static void initialize(Loader loader) {
        instance = loader;

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        Log.log("Welcome To PotionCraft!");

        FluidHelper.register();
        Config.loadConfig(loader.getConfigFile());

        PotionCauldronBlock.register();

        FabricMetaMixing.register();

        //OnUseData.register();
    }
}
