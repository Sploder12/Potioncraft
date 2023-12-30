package net.sploder12.potioncraft.meta;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;

// class for cauldron item interactions
public class BlockData {
    public Block source;
    public boolean valid; // in case of non-water leveled cauldron
    public PotionCauldronBlockEntity entity;
    public BlockState state;
    public int level;

    public BlockData() {
        source = Blocks.AIR;
        valid = false;
        entity = null;
        state = null;
        level = 0;
    }

    // from leveled cauldron
    private BlockData(BlockState bstate, BlockPos pos, LeveledCauldronBlock leveledBlock) {
        source = bstate.getBlock();

        valid = true;
        level = bstate.get(LeveledCauldronBlock.LEVEL);
        state = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
        entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, state);

        assert entity != null;

        entity.setLevel(level);
    }

    // from empty
    private BlockData(Block block, BlockPos pos) {
        source = block;

        valid = true;
        level = Blocks.CAULDRON == block ? 0 : 1;
        state = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
        entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, state);

        assert entity != null;

        entity.setLevel(level);
    }

    // potion block
    private BlockData(BlockState bstate, PotionCauldronBlockEntity bentity) {
        source = bstate.getBlock();
        valid = true;
        entity = bentity;
        level = bentity.getLevel();
        state = bstate;
    }

    public static BlockData getBlockData(BlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();

        if (block instanceof PotionCauldronBlock) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PotionCauldronBlockEntity pbentity) {
                return new BlockData(state, pbentity);
            }
        }

        if (block instanceof LeveledCauldronBlock leveledBlock) {
            return new BlockData(state, pos, leveledBlock);
        }

        if (block instanceof AbstractCauldronBlock) {
            return new BlockData(block, pos);
        }

        return new BlockData();
    }

    public static void itemUse(Hand hand, ItemStack in, PlayerEntity player, ItemStack out) {
        Item item = in.getItem();
        player.setStackInHand(hand, ItemUsage.exchangeStack(in, player, out));
        player.incrementStat(Stats.USE_CAULDRON);
        player.incrementStat(Stats.USED.getOrCreateStat(item));
    }
}
