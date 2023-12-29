package net.sploder12.potioncraft;

import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// class for cauldron item interactions
public class OnUseData {
    static public class BlockData {
        public boolean vanilla;
        public boolean valid; // in case of non-water leveled cauldron
        public PotionCauldronBlockEntity entity;
        public BlockState state;
        public int level;

        public BlockData() {
            vanilla = true;
            valid = false;
            entity = null;
            state = null;
            level = 0;
        }
    };

    private static BlockData dataFromLeveled(BlockState state, BlockPos pos, LeveledCauldronBlock leveledBlock) {
        BlockData out = new BlockData();
        out.vanilla = true;

        if (state.getBlock() != Blocks.WATER_CAULDRON) {
            out.valid = false;
            return out;
        }

        out.valid = true;
        out.level = state.get(LeveledCauldronBlock.LEVEL);
        out.state = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
        out.entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, out.state);

        assert out.entity != null;

        out.entity.setLevel(out.level);

        return out;
    }

    private static BlockData dataFromEmpty(BlockPos pos) {
        BlockData out = new BlockData();
        out.vanilla = true;

        out.valid = true;
        out.level = 0;
        out.state = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
        out.entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, out.state);

        assert out.entity != null;

        out.entity.setLevel(out.level);

        return out;
    }

    private static BlockData dataFromPotionEntity(BlockState state, PotionCauldronBlockEntity entity) {
        BlockData out = new BlockData();
        out.vanilla = false;
        out.valid = true;

        out.entity = entity;
        out.level = entity.getLevel();
        out.state = state;

        return out;
    }

    public static BlockData getBlockData(BlockState state, World world, BlockPos pos) {
        Block block = state.getBlock();

        if (block instanceof LeveledCauldronBlock leveledBlock) {
            return dataFromLeveled(state, pos, leveledBlock);
        }

        if (block instanceof CauldronBlock) {
            return dataFromEmpty(pos);
        }

        if (block instanceof PotionCauldronBlock) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PotionCauldronBlockEntity pbentity) {
                return dataFromPotionEntity(state, pbentity);
            }
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
