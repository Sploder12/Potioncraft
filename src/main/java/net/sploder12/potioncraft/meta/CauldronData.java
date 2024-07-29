package net.sploder12.potioncraft.meta;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;
import net.sploder12.potioncraft.util.HeatHelper;

// class for cauldron item interactions
public class CauldronData {
    final public Block source;
    final public int heat;
    final public PotionCauldronBlockEntity entity;

    public CauldronData(PotionCauldronBlockEntity entity, int heat) {
        this.source = PotionCauldronBlock.POTION_CAULDRON_BLOCK;
        this.heat = heat;
        this.entity = entity;
    }

    public CauldronData(Fluid fluid, Block source, BlockPos pos, int level, int heat) {
        this.source = source;
        this.heat = heat;

        BlockState state = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
        this.entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, state);

        assert entity != null;

        this.entity.setFluid(fluid);
        this.entity.setLevel(level);
    }

    public int getLevel() {
        return entity.getLevel();
    }

    public boolean addLevel(boolean dilute) {
        return entity.addLevel(dilute);
    }

    public boolean removeLevel() {
        return entity.removeLevel();
    }

    public Fluid getFluid() {
        return entity.getFluid();
    }

    public void setFluid(Fluid fluid) {
        entity.setFluid(fluid);
    }

    public BlockPos getPos() {
        return entity.getPos();
    }

    public boolean hasEffects() {
        return entity.hasEffects();
    }

    private void placePotionCauldron(World world) {
        BlockPos pos = getPos();
        world.setBlockState(pos, PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState());
        BlockEntity dest = world.getBlockEntity(pos);

        assert dest != null;

        dest.readNbt(entity.createNbt());
    }

    private void placeCauldron(World world) {
        BlockState block = FluidHelper.getBlock(getFluid()).getDefaultState();

        if (block.getBlock() instanceof LeveledCauldronBlock) {
            world.setBlockState(getPos(), block.with(LeveledCauldronBlock.LEVEL, getLevel()));
        }
        else if (getLevel() == PotionCauldronBlock.MAX_LEVEL) {
            world.setBlockState(getPos(), block);
        }
        else {
            // it's not a leveled cauldron but it needs levels!
            placePotionCauldron(world);
        }
    }

    public void transformBlock(World world, int initialLevel) {
        // turn empty cauldrons into cauldrons
        if (getFluid() == Fluids.EMPTY || getLevel() < PotionCauldronBlock.MIN_LEVEL) {
            if (source != Blocks.CAULDRON) {
                BlockState cauldron = Blocks.CAULDRON.getDefaultState();
                world.setBlockState(getPos(), cauldron);
            }
            return;
        }

        if (source == PotionCauldronBlock.POTION_CAULDRON_BLOCK) {
            if (!hasEffects()) {
                // turn potion cauldrons into normal cauldrons
                placeCauldron(world);
            }
            return;
        }

        // turn normal cauldrons into potion cauldrons
        if (hasEffects()) {
            placePotionCauldron(world);
            return;
        }

        // update existing cauldron
        if (getLevel() != initialLevel) {
            placeCauldron(world);
        }
    }

    protected static CauldronData fromLeveledCauldron(BlockState state, World world, BlockPos pos, int heat) {
        int level = state.get(LeveledCauldronBlock.LEVEL);
        Fluid fluid = FluidHelper.getFluid(state, world, pos);

        return new CauldronData(fluid, state.getBlock(), pos, level, heat);
    }

    protected static CauldronData fromNonLeveledCauldron(BlockState state, World world, BlockPos pos, int heat) {
        int level = state.getBlock() == Blocks.CAULDRON ? 0 : PotionCauldronBlock.MAX_LEVEL;
        Fluid fluid = FluidHelper.getFluid(state, world, pos);

        return new CauldronData(fluid, state.getBlock(), pos, level, heat);
    }

    protected static CauldronData fromPotionCauldron(PotionCauldronBlockEntity entity, int heat) {
        return new CauldronData(entity, heat);
    }

    public static CauldronData from(BlockState state, World world, BlockPos pos) {

        int heat = HeatHelper.getHeatOf(state, world, pos);

        Block block = state.getBlock();
        if (block instanceof PotionCauldronBlock) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PotionCauldronBlockEntity pbentity) {
                return fromPotionCauldron(pbentity, heat);
            }
        }

        if (block instanceof LeveledCauldronBlock) {
            return fromLeveledCauldron(state, world, pos, heat);
        }

        if (block instanceof AbstractCauldronBlock) {
            return fromNonLeveledCauldron(state, world, pos, heat);
        }

        return null;
    }

    public static void itemUse(World world, BlockPos pos, Hand hand, ItemStack in, PlayerEntity player, ItemStack out, int count) {
        if (count == 0) return;

        Item item = in.getItem();

        if (count > 1) {
            in.decrement(count - 1);
        }

        if (player != null) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(in, player, out));

            player.incrementStat(Stats.USE_CAULDRON);
            player.increaseStat(Stats.USED.getOrCreateStat(item), count);
        }
        else {
            // ItemUsage.exchangeStack internally decrements by 1, but this doesn't have a player
            in.decrement(1);

            if (out != null && !out.isEmpty()) {
                ItemEntity outEntity = new ItemEntity(world, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, out);
                outEntity.addVelocity(0.0f, 0.2f, 0.0f);
                world.spawnEntity(outEntity);
            }
        }
    }
}
