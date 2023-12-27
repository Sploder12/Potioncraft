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

    public static Map<Item, CauldronBehavior> interactions = CauldronBehavior.createMap();

    public static CauldronBehavior addInteraction(Item item, CauldronBehavior func, boolean water) {
        if (water) {
            interactions.put(item, func);
            return CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(item, func);
        }
        else {
            return interactions.put(item, func);
        }
    }

    public static void register() {
        Main.log("Registering Item Interactions...");

        addInteraction(Items.REDSTONE, OnUseData::redstoneOnUse, false);
        addInteraction(Items.GLOWSTONE_DUST, OnUseData::glowstoneOnUse, false);

        // Reagents

        addInteraction(Items.SUGAR, true,
                new PotionEffectInstance(Potions.SWIFTNESS));

        addInteraction(Items.RABBIT_FOOT, true,
                new PotionEffectInstance(Potions.LEAPING));

        addInteraction(Items.GLISTERING_MELON_SLICE, true,
                new PotionEffectInstance(Potions.HEALING));

        addInteraction(Items.SPIDER_EYE, true,
                new PotionEffectInstance(Potions.POISON));

        addInteraction(Items.PUFFERFISH, true,
                new PotionEffectInstance(Potions.WATER_BREATHING));

        addInteraction(Items.MAGMA_CREAM, true,
                new PotionEffectInstance(Potions.FIRE_RESISTANCE));

        addInteraction(Items.GOLDEN_CARROT, true,
                new PotionEffectInstance(Potions.NIGHT_VISION));

        addInteraction(Items.BLAZE_POWDER, true,
                new PotionEffectInstance(Potions.STRENGTH));

        addInteraction(Items.GHAST_TEAR, true,
                new PotionEffectInstance(Potions.REGENERATION));

        // sorry, no turtle master

        addInteraction(Items.PHANTOM_MEMBRANE, true,
                new PotionEffectInstance(Potions.SLOW_FALLING));
    }

    public static void addInteraction(Item item, boolean water, PotionEffectInstance effect) {
        addInteraction(item, buildBasicUse(effect), water);
    }

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

    private static CauldronBehavior buildBasicUse(PotionEffectInstance effect) {
        return (BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack itemStack) -> {
            if (!Config.canUseReagents) {
                return ActionResult.PASS;
            }

            BlockData data = getBlockData(state, world, pos);
            if (!data.valid) {
                return ActionResult.PASS;
            }

            if (!world.isClient) {
                PotionEffectInstance eCopy = new PotionEffectInstance(effect);

                float levelDilution = 1.0f / data.level;
                data.entity.addEffect(levelDilution * data.entity.getEffectNerf(effect.type), eCopy);

                itemUse(hand, itemStack, player, ItemStack.EMPTY);
                world.playSound((PlayerEntity)null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

                if (data.vanilla && !data.entity.getEffects().isEmpty()) {
                    world.setBlockState(pos, data.state);
                    BlockEntity dest = world.getBlockEntity(pos);
                    assert dest != null;

                    dest.readNbt(data.entity.createNbt());
                }
            }

            return ActionResult.success(world.isClient);
        };
    }

    private static ActionResult redstoneOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack red) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            itemUse(hand, red, player, ItemStack.EMPTY);
            world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

            data.entity.extendDuration(6000.0f);
        }

        return ActionResult.success(world.isClient);
    }

    private static ActionResult glowstoneOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack glow) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            itemUse(hand, glow, player, ItemStack.EMPTY);
            world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

            data.entity.amplify(3.0f);
        }

        return ActionResult.success(world.isClient);
    }
}
