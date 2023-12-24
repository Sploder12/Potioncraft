package net.sploder12.potioncraft;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Scanner;

public class Config {
    static final String filename = "potioncraft/potioncraft";

    private static File configFile = null;

    // Config Fields

    static boolean debug = false;
    static final String debugStr = "debug";

    static boolean allowMixing = true;
    static final String allowMixingStr = "allow_mixing";

    static boolean canUseReagents = true;
    static final String canUseReagentsStr = "can_use_reagents";

    static void loadDefaults() {
        debug = false;
        allowMixing = true;
        canUseReagents = true;
    }

    static void resetConfig() {
        loadDefaults();

        try {
            saveConfig();
        }
        catch (IOException e) {
            Main.log("Config failed to save! " + e);
        }
    }


    static void loadConfig() {
        if (configFile == null) {
            Path path = FabricLoader.getInstance().getConfigDir();
            configFile = path.resolve(filename + ".properties").toFile();
        }

        // raw config data
        HashMap<String, String> config = new HashMap<>();

        loadDefaults();

        try (Scanner ifstream = new Scanner(configFile)) {
            while(ifstream.hasNextLine()) {
                String line = ifstream.nextLine();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                String[] data = line.split("=", 2);
                if (data.length >= 2) {
                    config.put(data[0], data[1]);
                } // ignore length 1 data
            }
        }
        catch (FileNotFoundException e) {
            resetConfig();
            return;
        }

        boolean containsAll = true;

        // read and parse all data from config
        if (config.containsKey(debugStr)) {
           debug = Boolean.parseBoolean(config.get(debugStr));
        }
        else {
            containsAll = false;
        }

        if (config.containsKey(allowMixingStr)) {
            allowMixing = Boolean.parseBoolean(config.get(allowMixingStr));
        }
        else {
            containsAll = false;
        }

        if (config.containsKey(canUseReagentsStr)) {
            canUseReagents = Boolean.parseBoolean(config.get(canUseReagentsStr));
        }
        else {
            containsAll = false;
        }

        // config is missing some properties, probably out of date
        if (!containsAll) {
            try {
                saveConfig();
            }
            catch (IOException e) {
                Main.log("Config failed to save! " + e);
            }
        }
    }

    private static FileWriter writeBool(FileWriter writer, boolean bool) throws IOException {
        if (bool) {
            writer.write("true\n\n");
        }
        else {
            writer.write("false\n\n");
        }
        return writer;
    }


    static void saveConfig() throws IOException {
        if (configFile == null) {
            Path path = FabricLoader.getInstance().getConfigDir();
            configFile = path.resolve(filename + ".properties").toFile();
        }

        configFile.getParentFile().mkdirs();
        configFile.createNewFile();

        try (FileWriter ofstream = new FileWriter(configFile)) {

            ofstream.write("#Potioncraft Config\n");
            ofstream.write("#Timestamp: ");
            ofstream.write(LocalDateTime.now() + "\n\n");

            ofstream.write("#Should Potion Mixing be Possible?\n");
            ofstream.write(allowMixingStr + '=');
            writeBool(ofstream, allowMixing);

            ofstream.write("#Should Adding Reagents to Mixtures be Possible?\n");
            ofstream.write(canUseReagentsStr + '=');
            writeBool(ofstream, canUseReagents);
        }
    }


}
