package net.sploder12.potioncraft.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sploder12.potioncraft.common.config.Config;

public class Log {
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
}
