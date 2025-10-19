package org.sploder.potioncraft.common.meta.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.util.Json;

import java.util.HashMap;

public class InversionMapping {
    HashMap<StatusEffect, StatusEffect> inversions = new HashMap<>();

    public void addMutualInversion(StatusEffect first, StatusEffect second) {
        inversions.put(first, second);
        inversions.put(second, first);
    }

    public void addInversion(StatusEffect from, StatusEffect to) {
        inversions.put(from, to);
    }

    public boolean has(StatusEffect effect) {
        return inversions.containsKey(effect);
    }

    public StatusEffect get(StatusEffect effect) {
        return inversions.get(effect);
    }

    public static InversionMapping parse(JsonElement elem, String file) {
        InversionMapping out = new InversionMapping();
        if (elem == null) {
            Log.debug("inversions not present " + file);
            return out;
        }

        if (!elem.isJsonArray()) {
            Log.warn("inversions is not an array " + file);
            return out;
        }

        parseInversions(out, elem.getAsJsonArray(), file);
        return out;
    }

    private static void parseInversions(InversionMapping out, JsonArray inversions, String id) {
        for (JsonElement inversionE : inversions) {
            if (inversionE.isJsonObject()) {
                JsonObject inversion = inversionE.getAsJsonObject();

                Identifier from = Json.getId(inversion.get("from"));

                Identifier to = Json.getId(inversion.get("to"));

                if (from == null || to == null || from.equals(to)) {
                    Log.warn("invalid inversion between " + from + " and " + to + " " + id);
                    continue;
                }

                boolean mutual = Json.getBoolOr(inversion.get("mutual"), false);

                var fromE = Registries.STATUS_EFFECT.get(from);
                var toE = Registries.STATUS_EFFECT.get(to);

                // note: the default effect returned is luck.
                // therefore there is no way to determine if it is valid or not.

                if (mutual) {
                    out.addMutualInversion(fromE, toE);
                }
                else {
                    out.addInversion(fromE, toE);
                }
            }
        }
    }
}
