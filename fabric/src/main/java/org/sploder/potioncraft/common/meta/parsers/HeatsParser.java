package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.util.HeatHelper;

public interface HeatsParser {
    private static void parseHeats(JsonObject heats, String id) {
        heats.asMap().forEach((String blockStr, JsonElement obj) -> {
            if (!obj.isJsonPrimitive()) {
                return;
            }

            JsonPrimitive prim = obj.getAsJsonPrimitive();
            if (!prim.isNumber()) {
                return;
            }

            int heat = prim.getAsInt();

            Identifier blockId = Identifier.tryParse(blockStr);
            if (blockId == null) {
                Log.warn("block " + blockStr + " is not an identifier " + id);
                return;
            }

            var block = Registries.BLOCK.get(blockId);
            if (block == Blocks.AIR && !blockId.getPath().equalsIgnoreCase("air")) {
                Log.warn("block " + blockStr + " is not a block identifier " + id);
                return;
            }

            HeatHelper.addStaticMapping(block, heat);
        });
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            Log.debug("heats not present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Log.warn("heats resource not object " + file);
            return;
        }

        parseHeats(elem.getAsJsonObject(), file);
    }
}
