package net.sploder12.potioncraft;

import net.minecraft.block.BlockState;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

public class FluidHelper {
    public static class BlockInfo {
        public BlockState state;
        public World world;
        public BlockPos pos;

        BlockInfo(BlockState state, World world, BlockPos pos) {
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        public BlockEntity getBlockEntity() {
            return world.getBlockEntity(pos);
        }
    }

    private static final HashMap<Identifier, Function<BlockInfo, Fluid>> associatedFluids = new HashMap<>();
    private static final HashMap<Fluid, Block> associatedCauldrons = new HashMap<>();

    public static Fluid getFluid(Identifier id, BlockState state, World world, BlockPos pos) {
        if (associatedFluids.containsKey(id)) {
            return associatedFluids.get(id).apply(new BlockInfo(state, world, pos));
        }

        return Fluids.EMPTY;
    }

    public static Fluid getFluid(BlockState state, World world, BlockPos pos) {
        return getFluid(Registries.BLOCK.getId(state.getBlock()), state, world, pos);
    }

    public static void setFluidAssociation(Identifier id, Function<BlockInfo, Fluid> association) {
        associatedFluids.put(id, association);
    }

    public static Block getBlock(Fluid fluid) {
        if (associatedCauldrons.containsKey(fluid)) {
            return associatedCauldrons.get(fluid);
        }

        return Blocks.CAULDRON;
    }

    public static void setCauldronAssociation(Fluid fluid, Block cauldron) {
        associatedCauldrons.put(fluid, cauldron);
    }

    public static void register() {
        setFluidAssociation(Registries.BLOCK.getId(Blocks.WATER_CAULDRON), (BlockInfo info) -> Fluids.WATER);
        setFluidAssociation(Registries.BLOCK.getId(Blocks.LAVA_CAULDRON), (BlockInfo info) -> Fluids.LAVA);
        setFluidAssociation(PotionCauldronBlock.POTION_CAULDRON_ID, (BlockInfo info) -> {
            if (info.getBlockEntity() instanceof PotionCauldronBlockEntity entity) {
                return entity.getFluid();
            }
            return Fluids.EMPTY;
        });

        setCauldronAssociation(Fluids.EMPTY, Blocks.CAULDRON);
        setCauldronAssociation(Fluids.WATER, Blocks.WATER_CAULDRON);
        setCauldronAssociation(Fluids.LAVA, Blocks.LAVA_CAULDRON);

        // these shouldn't really exist, but they exist just in case
        setCauldronAssociation(Fluids.FLOWING_WATER, Blocks.WATER_CAULDRON);
        setCauldronAssociation(Fluids.FLOWING_LAVA, Blocks.LAVA_CAULDRON);

        // powdered snow is not a fluid...
    }

    public static Fluid getStill(Fluid fluid) {
        Objects.requireNonNull(fluid, "Fluid may not be null.");

        if (!fluid.isStill(fluid.getDefaultState())) {
            if (fluid instanceof FlowableFluid flowable) {
                return flowable.getStill();
            }
        }

        return fluid;
    }
}
