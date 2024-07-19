package net.sploder12.potioncraft.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.sploder12.potioncraft.PotionCauldronBlock;

import java.util.HashMap;
import java.util.function.Function;

public class HeatHelper {
    private static final HashMap<Block, Function<WorldBlock, Integer>> heatMappings = new HashMap<>();

    public static final int DEFAULT_HEAT = 0;

    // returns the heating value from THIS block
    public static int getHeatFrom(WorldBlock block) {
        Function<WorldBlock, Integer> mapping = heatMappings.get(block.getBlock());

        if (mapping != null) {
            Integer heat = mapping.apply(block);
            return (heat == null) ? DEFAULT_HEAT : heat;
        }

        return DEFAULT_HEAT;
    }

    // returns the heating value from THIS block
    public static int getHeatFrom(BlockState state, World world, BlockPos pos) {
        return getHeatFrom(new WorldBlock(state, world, pos));
    }

    // returns the heat based on the block below this one
    public static int getHeatOf(BlockState state, World world, BlockPos pos) {
        BlockPos belowPos = pos.down();
        BlockState below = world.getBlockState(belowPos);
        return getHeatFrom(below, world, belowPos);
    }

    public static Function<WorldBlock, Integer> addStaticMapping(Block block, int heat) {
        return heatMappings.put(block, (WorldBlock info) -> heat);
    }

    // adds a dynamic mapping to the heat mappings, it is NOT safe to assume WorldBlock.block == Block
    public static Function<WorldBlock, Integer> addMapping(Block block, Function<WorldBlock, Integer> mapping) {
        return heatMappings.put(block, mapping);
    }


    public static void reset() {
        heatMappings.clear();
        register();
    }

    public static void register() {
        addMapping(PotionCauldronBlock.POTION_CAULDRON_BLOCK, (WorldBlock info) -> {
           Block effectiveBlock = FluidHelper.getBlock(FluidHelper.getFluid(info));

           Function<WorldBlock, Integer> mapping = heatMappings.get(effectiveBlock);
           if (mapping != null) {
               return mapping.apply(info);
           }

           return DEFAULT_HEAT;
        });
    }
}
