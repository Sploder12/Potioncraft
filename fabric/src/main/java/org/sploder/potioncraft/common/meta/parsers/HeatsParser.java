package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.util.HeatHelper;
import org.sploder.potioncraft.common.util.WorldBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public interface HeatsParser {
    private static Block parseBlock(String blockStr, String id) {
        Identifier blockId = Identifier.tryParse(blockStr);
        if (blockId == null) {
            Log.warn("block " + blockStr + " is not an identifier " + id);
            return null;
        }

        var block = Registries.BLOCK.get(blockId);
        if (block == Blocks.AIR && !blockId.getPath().equalsIgnoreCase("air")) {
            Log.warn("block " + blockStr + " is not a block identifier " + id);
            return null;
        }

       return block;
    }

    @SuppressWarnings({"unchecked"})
    private static <T extends Comparable<T>> Optional<T> getProperty(Property<?> property, String valueStr) {
        Property<T> typed = (Property<T>) property;
        return typed.parse(valueStr);
    }

    private static void parseHeat(String blockstate, int heat, String id) {
        var stateStart = blockstate.indexOf('[');
        String state = null;

        if (stateStart != -1) {
            if (!blockstate.endsWith("]")) {
                Log.warn("blockstate " + blockstate + " has invalid [] " + id);
                return;
            }

            state = blockstate.substring(stateStart + 1, blockstate.length() - 1);
            blockstate = blockstate.substring(0, stateStart);
        }

        var block = parseBlock(blockstate, id);
        if (block == null) {
            return;
        }

        if (state == null) {
            HeatHelper.addStaticMapping(block, heat);
        }
        else {
            Function<WorldBlock, Integer> mapping = null;

            ArrayList<Pair<Property<?>, ? extends Comparable<?>>> proplist = new ArrayList<>();

            BlockState compareState = block.getDefaultState();
            String[] properties = state.split(",");
            for (String property : properties) {
                String[] pair = property.split("=");
                if (pair.length != 2) {
                    Log.warn("blockstate " + state + " has invalid property" + id);
                    continue;
                };

                String key = pair[0];
                String value = pair[1];

                Property<?> prop = compareState.getProperties().stream()
                        .filter(p -> p.getName().equals(key))
                        .findFirst()
                        .orElse(null);

                if (prop == null) {
                    Log.warn("block " + blockstate + " has no property \"" + key + "\"" + state + " has invalid property" + id);
                    continue;
                }

                var v = getProperty(prop, value);
                if (v.isEmpty()) {
                    Log.warn("\"" + value + "\" is not a valid value for \"" + key + "\"" + id);
                    continue;
                }

                proplist.add(new Pair<>(prop, v.get()));
            }

            HeatHelper.addMapping(block, (WorldBlock b) -> {
                for (var prop : proplist) {
                    if (!b.state.get(prop.getLeft()).equals(prop.getRight())) {
                        return null;
                    }
                }

                return heat;
            });
        }
    }

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

            parseHeat(blockStr, heat, id);
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
