package net.sploder12.potioncraft.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;

import java.util.*;
import java.util.function.Function;

public class FluidHelper {

    private static final HashMap<Block, Function<WorldBlock, Fluid>> blockMappings = new HashMap<>();
    private static final HashMap<Fluid, DefaultedHashSet<Block>> fluidMappings = new HashMap<>();

    public static Fluid getFluid(WorldBlock block) {
        Function<WorldBlock, Fluid> mapping = blockMappings.get(block.getBlock());

        if (mapping != null) {
            return mapping.apply(block);
        }

        return Fluids.EMPTY;
    }

    public static Fluid getFluid(BlockState state, World world, BlockPos pos) {
        return getFluid(new WorldBlock(state, world, pos));
    }

    public static void setBlockMapping(Block id, Function<WorldBlock, Fluid> association) {
        blockMappings.put(id, association);
    }

    public static void setStaticBlockMapping(Block id, Fluid fluid) {
        setBlockMapping(id, (WorldBlock info) -> fluid);
    }

    public static Block getBlock(Fluid fluid) {
        DefaultedHashSet<Block> cauldrons = fluidMappings.get(fluid);

        if (cauldrons != null) {
            return cauldrons.getDefaultElement();
        }

        return Blocks.CAULDRON;
    }

    public static void setDefaultFluidMapping(Fluid fluid, Block cauldron) {
        DefaultedHashSet<Block> cauldrons = fluidMappings.get(fluid);

        if (cauldrons == null) {
            fluidMappings.put(fluid, new DefaultedHashSet<>(cauldron));
        }
        else {
            cauldrons.setDefault(cauldron);
        }
    }

    public static void addFluidMapping(Fluid fluid, Block cauldron) {
        DefaultedHashSet<Block> cauldrons = fluidMappings.get(fluid);

        if (cauldrons == null) {
            fluidMappings.put(fluid, new DefaultedHashSet<>(cauldron));
        }
        else {
            cauldrons.add(cauldron);
        }
    }

    public static Fluid getStill(Fluid fluid) {
        Objects.requireNonNull(fluid, "Fluid may not be null.");

        if (!fluid.isStill(fluid.getDefaultState())) {
            if (fluid instanceof FlowableFluid flowable) {
                return flowable.getStill();
            }

            Main.log("Fluid, " + Registries.FLUID.getId(fluid) + ", cannot be still!");
        }

        return fluid;
    }

    public static void reset() {
        fluidMappings.clear();
        blockMappings.clear();
        register();
    }

    public static void register() {
        setStaticBlockMapping(Blocks.WATER_CAULDRON, Fluids.WATER);
        setStaticBlockMapping(Blocks.LAVA_CAULDRON, Fluids.LAVA);
        setBlockMapping(PotionCauldronBlock.POTION_CAULDRON_BLOCK, (WorldBlock info) -> {
            if (info.getBlockEntity() instanceof PotionCauldronBlockEntity entity) {
                return entity.getFluid();
            }
            return Fluids.EMPTY;
        });

        setDefaultFluidMapping(Fluids.EMPTY, Blocks.CAULDRON);
        setDefaultFluidMapping(Fluids.WATER, Blocks.WATER_CAULDRON);
        setDefaultFluidMapping(Fluids.LAVA, Blocks.LAVA_CAULDRON);

        // these are very important for generating interactions!
        addFluidMapping(Fluids.WATER, PotionCauldronBlock.POTION_CAULDRON_BLOCK);
        addFluidMapping(Fluids.LAVA, PotionCauldronBlock.POTION_CAULDRON_BLOCK);

        // these shouldn't really exist, but they exist just in case
        setDefaultFluidMapping(Fluids.FLOWING_WATER, Blocks.WATER_CAULDRON);
        setDefaultFluidMapping(Fluids.FLOWING_LAVA, Blocks.LAVA_CAULDRON);

        // powdered snow is not a fluid...
    }
}
