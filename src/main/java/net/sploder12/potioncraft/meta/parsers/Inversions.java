package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.util.Json;

import java.util.HashMap;

public interface Inversions {
    HashMap<StatusEffect, StatusEffect> inversions = new HashMap<>();

    static void addMutualInversion(StatusEffect first, StatusEffect second) {
        inversions.put(first, second);
        inversions.put(second, first);
    }

    static void addInversion(StatusEffect from, StatusEffect to) {
        inversions.put(from, to);
    }

    static void clear() {
        inversions.clear();
    }

    private static void parseInversions(JsonArray inversions, String id) {
        for (JsonElement inversionE : inversions) {
            if (inversionE.isJsonObject()) {
                JsonObject inversion = inversionE.getAsJsonObject();

                Identifier from = Json.getId(inversion.get("from"));

                Identifier to = Json.getId(inversion.get("to"));

                if (from == null || to == null || from.equals(to)) {
                    Main.log("WARNING: invalid inversion in " + id);
                    continue;
                }

                boolean mutual = Json.getBoolOr(inversion.get("mutual"), false);

                StatusEffect fromE = Registries.STATUS_EFFECT.get(from);
                StatusEffect toE = Registries.STATUS_EFFECT.get(to);

                // note: the default effect returned is luck.
                // therefore there is no way to determine if it is valid or not.

                if (mutual) {
                    addMutualInversion(fromE, toE);
                }
                else {
                    addInversion(fromE, toE);
                }
            }
        }
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null) {
            return;
        }

        if (!elem.isJsonArray()) {
            Main.log("WARNING: inversions is not an array " + file);
            return;
        }

        parseInversions(elem.getAsJsonArray(), file);
    }
}
