package net.sploder12.potioncraft;

import net.fabricmc.api.ModInitializer;

import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.meta.MetaMixing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("PotionCraft");

    public static void debug(String str) {
        if (Config.getBoolean(Config.FieldID.DEBUG)) {
            LOGGER.debug(str);
        }
    }

    public static void log(String str) {
        LOGGER.info(str);
    }

    public static void warn(String str) {
        LOGGER.warn("WARNING: " + str);
    }

    public static void error(String str) {
        LOGGER.error("ERROR: " + str);
    }

    public static void error(String str, Throwable except) {
        LOGGER.error("ERROR: " + str, except);
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        log("Welcome To PotionCraft!");

        FluidHelper.register();
        Config.loadConfig();

        PotionCauldronBlock.register();

        MetaMixing.register();

        //OnUseData.register();
    }
}

