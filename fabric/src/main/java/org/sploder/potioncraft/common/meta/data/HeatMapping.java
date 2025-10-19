package org.sploder.potioncraft.common.meta.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.PotionCauldronBlock;
import org.sploder.potioncraft.common.util.BlockProperties;
import org.sploder.potioncraft.common.util.FluidHelper;
import org.sploder.potioncraft.common.util.WorldBlock;

import java.util.HashMap;
import java.util.function.Function;

public class HeatMapping {
    public static final int DEFAULT_HEAT = 0;

    private final HashMap<Block, Function<WorldBlock, Integer>> mappings = new HashMap<>();

    // returns the heating value from THIS block
    public int getHeatFrom(WorldBlock block) {
        var mapping = mappings.get(block.getBlock());

        if (mapping != null) {
            Integer heat = mapping.apply(block);
            return (heat == null) ? DEFAULT_HEAT : heat;
        }

        return DEFAULT_HEAT;
    }

    // returns the heating value from THIS block
    public int getHeatFrom(BlockState state, World world, BlockPos pos) {
        return getHeatFrom(new WorldBlock(state, world, pos));
    }

    // returns the heat based on the block below this one
    public int getHeatOf(BlockState state, World world, BlockPos pos) {
        BlockPos belowPos = pos.down();
        BlockState below = world.getBlockState(belowPos);
        return getHeatFrom(below, world, belowPos);
    }

    public Function<WorldBlock, Integer> getMapping(Block block) {
        return mappings.get(block);
    }

    // adds a dynamic mapping to the heat mappings, it is NOT safe to assume WorldBlock.block == Block
    public Function<WorldBlock, Integer> setMapping(Block block, Function<WorldBlock, Integer> mapping) {
        return mappings.put(block, mapping);
    }

    // adds a dynamic mapping to the heat mappings, it is NOT safe to assume WorldBlock.block == Block
    public void addMapping(Block block, Function<WorldBlock, Integer> mapping) {
        var prev = mappings.get(block);
        if (prev != null) {
            setMapping(block, (WorldBlock info) -> {
                var pres = prev.apply(info);
                if (pres != null) {
                    return pres;
                }
                return mapping.apply(info);
            });
            return;
        }
        setMapping(block, mapping);
    }

    public static HeatMapping makeMapping() {
        HeatMapping out = new HeatMapping();
        out.addMapping(PotionCauldronBlock.POTION_CAULDRON_BLOCK, (WorldBlock info) -> {
            var effectiveBlock = FluidHelper.getBlock(FluidHelper.getFluid(info));

            var mapping = out.getMapping(effectiveBlock);
            if (mapping != null) {
                return mapping.apply(info);
            }

            return DEFAULT_HEAT;
        });
        return out;
    }

    public static HeatMapping parse(JsonElement elem, String file) {
        HeatMapping out = makeMapping();
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

    private static void parseHeats(HeatMapping out, JsonObject heats, String id) {
        heats.asMap().forEach((String blockStr, JsonElement obj) -> {
            if (!obj.isJsonPrimitive()) {
                return;
            }

            final JsonPrimitive prim = obj.getAsJsonPrimitive();
            if (!prim.isNumber()) {
                return;
            }

            int heat = prim.getAsInt();

            parseHeat(out, blockStr, heat, id);
        });
    }

    private static void parseHeat(HeatMapping out, String blockstate, int heat, String id) {
        try {
            final BlockProperties proplist = new BlockProperties(blockstate);
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
}
