package net.sploder12.potioncraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.mixin.LeveledAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

// class for cauldron item interactions
public class OnUseData {

    public static Map<Item, CauldronBehavior> interactions = CauldronBehavior.createMap();

    public static CauldronBehavior addInteraction(Item item, CauldronBehavior func) {
        return interactions.put(item, func);
    }

    public static void register() {
        Main.log("Registering Item Interactions...");

        addInteraction(Items.POTION, OnUseData::potionOnUse);
        addInteraction(Items.GLASS_BOTTLE, OnUseData::bottleOnUse);

        addInteraction(Items.MILK_BUCKET, OnUseData::milkOnUse);

        addInteraction(Items.FERMENTED_SPIDER_EYE, OnUseData::fermSpiderEyeOnUse);

        addInteraction(Items.REDSTONE, OnUseData::redstoneOnUse);
        addInteraction(Items.GLOWSTONE_DUST, OnUseData::glowstoneOnUse);

        // Reagents

        addInteraction(Items.SUGAR, false,
                new PotionEffectInstance(Potions.SWIFTNESS));

        addInteraction(Items.RABBIT_FOOT, false,
                new PotionEffectInstance(Potions.LEAPING));

        addInteraction(Items.GLISTERING_MELON_SLICE, false,
                new PotionEffectInstance(Potions.HEALING));

        addInteraction(Items.SPIDER_EYE, false,
                new PotionEffectInstance(Potions.POISON));

        addInteraction(Items.PUFFERFISH, false,
                new PotionEffectInstance(Potions.WATER_BREATHING));

        addInteraction(Items.MAGMA_CREAM, false,
                new PotionEffectInstance(Potions.FIRE_RESISTANCE));

        addInteraction(Items.GOLDEN_CARROT, false,
                new PotionEffectInstance(Potions.NIGHT_VISION));

        addInteraction(Items.BLAZE_POWDER, false,
                new PotionEffectInstance(Potions.STRENGTH));

        addInteraction(Items.GHAST_TEAR, false,
                new PotionEffectInstance(Potions.REGENERATION));

        // sorry, no turtle master

        addInteraction(Items.PHANTOM_MEMBRANE, false,
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

    public static void addInteraction(Item item, Boolean ignoreVanilla, PotionEffectInstance effect) {
        addInteraction(item, buildBasicUse(ignoreVanilla, effect));
    }

    public static ItemStack itemUse(int amount, PlayerEntity player, Hand hand, ItemStack items, @Nullable ItemStack replace) {
        if (!player.isCreative()) {
            items.decrement(amount);
        }

        if (replace != null) {
            if (items.isEmpty()) {
                player.setStackInHand(hand, replace);
            } else {
                /*
                   this is technically divergent from Vanilla;
                   in creative, vanilla will not give you a new item
                   if it already exists in the inventory.
                   I'm not doing this cause it's slow and nobody will notice.
                */

                player.getInventory().insertStack(replace);
            }
        }

        return items;
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

        if (!((LeveledAccessor)(leveledBlock)).callCanBeFilledByDripstone(Fluids.WATER)) {
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

        if (block instanceof PotionCauldronBlock) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof PotionCauldronBlockEntity pbentity) {
                return dataFromPotionEntity(state, pbentity);
            }
        }

        return new BlockData();
    }

    private static CauldronBehavior buildBasicUse(boolean ignoreVanilla, PotionEffectInstance effect) {
        return (BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack itemStack) -> {
            if (!Config.canUseReagents) {
                return ActionResult.PASS;
            }

            BlockData data = getBlockData(state, world, pos);
            if (!data.valid || (ignoreVanilla && data.vanilla)) {
                return ActionResult.PASS;
            }

            PotionEffectInstance eCopy = new PotionEffectInstance(effect);

            float levelDilution = 1.0f / data.level;
            data.entity.addEffect(levelDilution * data.entity.getEffectNerf(effect.type), eCopy);

            itemUse(1, player, hand, itemStack, null);

            if (data.vanilla && !data.entity.getEffects().isEmpty()) {
                world.setBlockState(pos, data.state);
                BlockEntity dest = world.getBlockEntity(pos);
                assert dest != null;

                dest.readNbt(data.entity.createNbt());
            }

            return ActionResult.SUCCESS;
        };
    }


    private static ActionResult potionOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack itemStack) {

        BlockData data = getBlockData(state, world, pos);
        if (!data.valid || data.level >= PotionCauldronBlock.MAX_LEVEL) {
            return ActionResult.PASS;
        }

        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);
        if (effects.isEmpty() && data.vanilla) {
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

        if (!data.entity.addLevel(effects)) {
            return ActionResult.PASS;
        }

        if (!effects.isEmpty() || data.vanilla) {
            itemUse(1, player, hand, itemStack, new ItemStack(Items.GLASS_BOTTLE));
        }

        return ActionResult.SUCCESS;
    }

    private static ActionResult bottleOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack bottles) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid || data.vanilla) {
            return ActionResult.PASS;
        }

        ItemStack potion = data.entity.pickupFluid();

        itemUse(1, player, hand, bottles, potion);

        // empty so force it to become cauldron
        if (data.entity.getLevel() < PotionCauldronBlock.MIN_LEVEL) {
            BlockState cauldron = Blocks.CAULDRON.getDefaultState();
            world.setBlockState(pos, cauldron);
        }

        return ActionResult.SUCCESS;
    }

    private static ActionResult milkOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack milk) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid || data.vanilla) {
            return ActionResult.PASS;
        }

        itemUse(1, player, hand, milk, new ItemStack(Items.BUCKET));


        if (data.level > 0) {
            BlockState cauldron = Blocks.WATER_CAULDRON.getDefaultState();
            world.setBlockState(pos, cauldron.with(LeveledCauldronBlock.LEVEL, data.level));
        }
        else {
            BlockState cauldron = Blocks.CAULDRON.getDefaultState();
            world.setBlockState(pos, cauldron);
        }

        return ActionResult.SUCCESS;
    }

    private static ActionResult fermSpiderEyeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack eye) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid || data.vanilla) {
            return ActionResult.PASS;
        }

        if (data.entity.invertEffects()) {
            itemUse(1, player, hand, eye, null);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private static ActionResult redstoneOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack red) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid || data.vanilla) {
            return ActionResult.PASS;
        }

        itemUse(1, player, hand, red, null);

        data.entity.extendDuration(6000.0f);

        return ActionResult.SUCCESS;
    }

    private static ActionResult glowstoneOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack glow) {
        BlockData data = getBlockData(state, world, pos);
        if (!data.valid || data.vanilla) {
            return ActionResult.PASS;
        }

        itemUse(1, player, hand, glow, null);

        data.entity.amplify(3.0f);

        return ActionResult.SUCCESS;
    }
}
