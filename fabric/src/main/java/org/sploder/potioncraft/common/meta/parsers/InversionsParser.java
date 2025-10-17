package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.meta.data.InversionMapping;
import org.sploder.potioncraft.common.util.Json;

public interface InversionsParser {

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

    static InversionMapping parse(JsonElement elem, String file) {
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
}
