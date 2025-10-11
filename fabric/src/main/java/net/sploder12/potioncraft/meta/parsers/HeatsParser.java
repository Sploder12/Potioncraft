package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.util.HeatHelper;

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
                Main.warn("block " + blockStr + " is not an identifier " + id);
                return;
            }

            Block block = Registries.BLOCK.get(blockId);
            if (block == Blocks.AIR && !blockId.getPath().equalsIgnoreCase("air")) {
                Main.warn("block " + blockStr + " is not a block identifier " + id);
                return;
            }

            HeatHelper.addStaticMapping(block, heat);
        });
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            Main.debug("heats not present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Main.warn("heats resource not object " + file);
            return;
        }

        parseHeats(elem.getAsJsonObject(), file);
    }
}
