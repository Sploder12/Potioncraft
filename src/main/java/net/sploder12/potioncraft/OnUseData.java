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

    public static HashMap<Identifier, Map<Item, CauldronBehavior>> customBehaviors = new HashMap<>();

    public static CauldronBehavior emptyAddWater = null;
    public static CauldronBehavior waterAddWater = null;

    public static Map<Item, CauldronBehavior> getBehavior(Identifier id) {
        if (id == PotionCauldronBlock.POTION_CAULDRON_ID) {
            return interactions;
        }

        if (id == Registries.BLOCK.getId(Blocks.CAULDRON)) {
            return CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR;
        }

        if (id == Registries.BLOCK.getId(Blocks.WATER_CAULDRON)) {
            return CauldronBehavior.WATER_CAULDRON_BEHAVIOR;
        }

        if (id == Registries.BLOCK.getId(Blocks.LAVA_CAULDRON)) {
            return CauldronBehavior.LAVA_CAULDRON_BEHAVIOR;
        }

        if (id == Registries.BLOCK.getId(Blocks.POWDER_SNOW_CAULDRON)) {
            return CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR;
        }

        if (customBehaviors.containsKey(id)) {
            return customBehaviors.get(id);
        }

        return null;
    }

    public static CauldronBehavior addInteraction(Identifier id, Item item, CauldronBehavior func) {
        Map<Item, CauldronBehavior> behavior = getBehavior(id);
        if (behavior == null) {
            return null;
        }

        return behavior.put(item, func);
    }


    public static void register() {
        Main.log("Registering Item Interactions...");

        if (waterAddWater == null) {
            waterAddWater = addInteraction(Items.POTION, OnUseData::potionOnUse, true);
        }
        else {
            addInteraction(Items.POTION, OnUseData::potionOnUse, true);
        }

        if (emptyAddWater == null) {
            emptyAddWater = addInteraction(Registries.BLOCK.getId(Blocks.CAULDRON), Items.POTION, OnUseData::potionOnUse);
        }
        else {
            addInteraction(Registries.BLOCK.getId(Blocks.CAULDRON), Items.POTION, OnUseData::potionOnUse);
        }


        addInteraction(Items.GLASS_BOTTLE, OnUseData::bottleOnUse, false);

        addInteraction(Items.MILK_BUCKET, OnUseData::milkOnUse, false);

        addInteraction(Items.FERMENTED_SPIDER_EYE, OnUseData::fermSpiderEyeOnUse, false);

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


        // Inversions
        PotionEffectInstance.addMutualInversion(
                StatusEffects.SPEED, StatusEffects.SLOWNESS);

        PotionEffectInstance.addMutualInversion(
                StatusEffects.JUMP_BOOST, StatusEffects.SLOW_FALLING);

        PotionEffectInstance.addMutualInversion(
                StatusEffects.INSTANT_HEALTH, StatusEffects.INSTANT_DAMAGE);

        PotionEffectInstance.addMutualInversion(
                StatusEffects.POISON, StatusEffects.REGENERATION);

        PotionEffectInstance.addMutualInversion(
                StatusEffects.NIGHT_VISION, StatusEffects.INVISIBILITY);

        PotionEffectInstance.addMutualInversion(
                StatusEffects.STRENGTH, StatusEffects.WEAKNESS);

    }

    public static void addInteraction(Item item, boolean water, PotionEffectInstance effect) {
        addInteraction(item, buildBasicUse(effect), water);
    }

    static private class BlockData {
        boolean vanilla;
        boolean valid; // in case of non-water leveled cauldron
        PotionCauldronBlockEntity entity;
        BlockState state;
        int level;

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

    private static BlockData getBlockData(BlockState state, World world, BlockPos pos) {
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

    private static void itemUse(World world, Hand hand, ItemStack in, PlayerEntity player, ItemStack out) {
        if (!world.isClient) {
            Item item = in.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(in, player, out));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
        }
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

                itemUse(world, hand, itemStack, player, ItemStack.EMPTY);
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


    private static ActionResult potionOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack itemStack) {

        BlockData data = getBlockData(state, world, pos);
        if (!data.valid || data.level >= PotionCauldronBlock.MAX_LEVEL) {
            return ActionResult.PASS;
        }

        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);
        if (effects.isEmpty() && data.vanilla) {
            if (data.level == 0) {
                return emptyAddWater.interact(state, world, pos, player, hand, itemStack);
            }

            return waterAddWater.interact(state, world, pos, player, hand, itemStack);
        }

        if (data.level >= PotionCauldronBlock.MAX_LEVEL) {
            return ActionResult.PASS;
        }

        // config disallowing mixing of separate potions (water will always be mixable)
        if (!Config.allowMixing && !effects.isEmpty() && !data.entity.getEffects().isEmpty()) {
            if (data.entity.getEffects().size() > 1 || effects.size() > 1) {
                return ActionResult.PASS;
            }

            if (data.entity.getEffects().get(0).getEffectType() != effects.get(0).getEffectType()) {
                return ActionResult.PASS;
            }
        }


        if (!world.isClient) {
            data.entity.addLevel(effects);

            if (data.vanilla) {
                world.setBlockState(pos, data.state);
                BlockEntity dest = world.getBlockEntity(pos);
                assert dest != null;

                dest.readNbt(data.entity.createNbt());
            }

            itemUse(world, hand, itemStack, player, new ItemStack(Items.GLASS_BOTTLE));
            world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, pos);
        }

        return ActionResult.success(world.isClient);
    }

    private static ActionResult bottleOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack bottles) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            ItemStack potion = data.entity.pickupFluid();
            itemUse(world, hand, bottles, player, potion);
            world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent((Entity) null, GameEvent.FLUID_PICKUP, pos);

            // empty so force it to become cauldron
            if (data.entity.getLevel() < PotionCauldronBlock.MIN_LEVEL) {
                BlockState cauldron = Blocks.CAULDRON.getDefaultState();
                world.setBlockState(pos, cauldron);
            }
        }

        return ActionResult.success(world.isClient);
    }

    private static ActionResult milkOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack milk) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            itemUse(world, hand, milk, player, new ItemStack(Items.BUCKET));
            world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (data.level > 0) {
                BlockState cauldron = Blocks.WATER_CAULDRON.getDefaultState();
                world.setBlockState(pos, cauldron.with(LeveledCauldronBlock.LEVEL, data.level));
            } else {
                BlockState cauldron = Blocks.CAULDRON.getDefaultState();
                world.setBlockState(pos, cauldron);
            }
        }

        return ActionResult.success(world.isClient);
    }

    private static ActionResult fermSpiderEyeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack eye) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid) {
            return ActionResult.PASS;
        }

        if (data.entity.invertEffects()) {
            itemUse(world, hand, eye, player, ItemStack.EMPTY);
            world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    private static ActionResult redstoneOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack red) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            itemUse(world, hand, red, player, ItemStack.EMPTY);
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
            itemUse(world, hand, glow, player, ItemStack.EMPTY);
            world.playSound((PlayerEntity) null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

            data.entity.amplify(3.0f);
        }

        return ActionResult.success(world.isClient);
    }
}
