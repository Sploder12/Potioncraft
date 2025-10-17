package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.meta.data.HeatMapping;
import org.sploder.potioncraft.common.util.BlockProperties;
import org.sploder.potioncraft.common.util.WorldBlock;

public interface HeatsParser {

    private static void parseHeat(HeatMapping out, String blockstate, int heat, String id) {
        try {
            BlockProperties proplist = new BlockProperties(blockstate);
            out.addMapping(proplist.getBlock(), (WorldBlock b) -> {
                if (!proplist.isEquivalent(b.state)) {
                    return null;
                }

                return heat;
            });
        }
        catch(IllegalArgumentException e) {
            Log.warn(e + id);
        }
    }

    private static void parseHeats(HeatMapping out, JsonObject heats, String id) {
        heats.asMap().forEach((String blockStr, JsonElement obj) -> {
            if (!obj.isJsonPrimitive()) {
                return;
            }

            JsonPrimitive prim = obj.getAsJsonPrimitive();
            if (!prim.isNumber()) {
                return;
            }

            int heat = prim.getAsInt();

            parseHeat(out, blockStr, heat, id);
        });
    }

    static HeatMapping parse(JsonElement elem, String file) {
        HeatMapping out = HeatMapping.makeMapping();
        if (elem == null || !elem.isJsonObject()) {
            Log.debug("heats not present " + file);
            return out;
        }

        if (!elem.isJsonObject()) {
            Log.warn("heats resource not object " + file);
            return out;
        }

        parseHeats(out, elem.getAsJsonObject(), file);
        return out;
    }
}
