package net.sploder12.potioncraft;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Scanner;

public class Config {
    static final String filename = "potioncraft/potioncraft";

    private static File configFile = null;

    // Internal identifiers for the fields
    public enum FieldID {
        DEBUG,
        ALLOW_MIXING,
        CAN_USE_REAGENTS,
        MAX_POTENCY,
        DEFAULT_POTION_POTENCY,
    };

    // Config Fields
    static HashMap<FieldID, Field> fields = new HashMap<>() {{
        put(FieldID.DEBUG, new BooleanField(false, "debug", "Debug mode enabled?"));
        put(FieldID.ALLOW_MIXING, new BooleanField(true, "allow_mixing", "Should Potion Mixing be Possible?"));
        put(FieldID.CAN_USE_REAGENTS, new BooleanField(true, "can_use_reagents", "Should Adding Reagents to Mixtures be Possible?"));
        put(FieldID.MAX_POTENCY, new IntField(5, "max_potency", "Maximum potency for crafted potions (negative for infinite)."));
        put(FieldID.DEFAULT_POTION_POTENCY, new IntField(1, "default_potion_potency", "Default potency for vanilla potions."));
    }};

    private static Field getField(FieldID id) {
        return fields.get(id);
    }

    public static Boolean getBoolean(FieldID id) {
        return Field.getBoolean(getField(id));
    }

    public static Integer getInteger(FieldID id) {
        return Field.getInteger(getField(id));
    }

    public static String getString(FieldID id) {
        return Field.getString(getField(id));
    }

    static void loadDefaults() {
        for (Field field : fields.values()) {
            field.reset();
        }
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
                if (line.isEmpty() || line.startsWith("#")) {
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
        for (Field field : fields.values()) {
            if (!field.load(config)) {
                containsAll = false;
            }
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

            for (Field field : fields.values()) {
                field.write(ofstream);
            }
        }
    }
}
