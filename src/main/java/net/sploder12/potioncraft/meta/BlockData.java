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
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.FluidHelper;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;

import java.util.HashMap;

// class for cauldron item interactions
public class BlockData {

    public static HashMap<Block, Integer> blockHeats = new HashMap<>();

    public Fluid fluid;
    public Block source;
    public boolean valid; // in case of non-water leveled cauldron
    public PotionCauldronBlockEntity entity;
    public BlockState state;
    public int level;
    public int heat;

    public BlockData() {
        fluid = Fluids.EMPTY;
        source = Blocks.AIR;
        valid = false;
        entity = null;
        state = null;
        level = 0;
        heat = 0;
    }

    // from leveled cauldron
    private BlockData(BlockState bstate, World world, BlockPos pos, LeveledCauldronBlock leveledBlock, int bheat) {
        source = bstate.getBlock();

        fluid = FluidHelper.getFluid(bstate, world, pos);
        valid = true;
        level = bstate.get(LeveledCauldronBlock.LEVEL);
        state = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
        entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, state);

        assert entity != null;

        entity.setFluid(fluid);
        entity.setLevel(level);
        heat = bheat;
    }

    // from other (non-leveled) cauldron
    private BlockData(BlockState block, World world, BlockPos pos, int bheat) {
        source = block.getBlock();

        fluid = FluidHelper.getFluid(block, world, pos);
        valid = true;
        level = Blocks.CAULDRON == block.getBlock() ? 0 : PotionCauldronBlock.MAX_LEVEL;
        state = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
        entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, state);

        assert entity != null;

        entity.setFluid(fluid);
        entity.setLevel(level);
        heat = bheat;
    }

    // potion block
    private BlockData(BlockState bstate, PotionCauldronBlockEntity bentity, int bheat) {
        fluid = bentity.getFluid();
        source = bstate.getBlock();
        valid = true;
        entity = bentity;
        level = bentity.getLevel();
        state = bstate;
        heat = bheat;
    }

    public static BlockData getBlockData(BlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();

        BlockState below = world.getBlockState(pos.add(0, -1, 0));
        int heat = 0;

        if (blockHeats.containsKey(below.getBlock())) {
            heat = blockHeats.get(below.getBlock());
        }

        if (block instanceof PotionCauldronBlock) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PotionCauldronBlockEntity pbentity) {
                return new BlockData(state, pbentity, heat);
            }
        }

        if (block instanceof LeveledCauldronBlock leveledBlock) {
            return new BlockData(state, world, pos, leveledBlock, heat);
        }

        if (block instanceof AbstractCauldronBlock) {
            return new BlockData(state, world, pos, heat);
        }

        return new BlockData();
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
