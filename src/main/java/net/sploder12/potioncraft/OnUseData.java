package net.sploder12.potioncraft;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

// class for cauldron item interactions
public class OnUseData {
    public PotionCauldronBlockEntity entity;
    public BlockState state;
    public World world;
    public BlockPos pos;
    public PlayerEntity user;
    public Hand hand;
    public BlockHitResult hit;
    public boolean fromPotionCauldron;

    public OnUseData(PotionCauldronBlockEntity entity, BlockState state, World world, BlockPos pos, PlayerEntity user, Hand hand, BlockHitResult hit, boolean fromPotionCauldron) {
        this.entity = entity;
        this.state = state;
        this.world = world;
        this.pos = pos;
        this.user = user;
        this.hand = hand;
        this.hit = hit;
        this.fromPotionCauldron = fromPotionCauldron;
    }

    public static void register() {
        Main.log("Registering Item Interactions...");

        PotionCauldronBlock.addInteraction(Items.POTION, OnUseData::potionOnUse);
        PotionCauldronBlock.addInteraction(Items.GLASS_BOTTLE, OnUseData::bottleOnUse);

        PotionCauldronBlock.addInteraction(Items.MILK_BUCKET, OnUseData::milkOnUse);

        addInteraction(Items.MAGMA_CREAM, false, new PotionEffectInstance(
                StatusEffects.FIRE_RESISTANCE,
                3600.0f,
                1.0f
        ));
    }

    public static void addInteraction(Item item, Boolean ignoreVanilla, PotionEffectInstance effect) {
        PotionCauldronBlock.addInteraction(item, buildBasicUse(ignoreVanilla, effect));
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

    private static Function<OnUseData, Boolean> buildBasicUse(boolean ignoreVanilla, PotionEffectInstance effect) {
        return (OnUseData data) -> {
            if (!Config.canUseReagents || (data.entity.getLevel() <= 0 || (ignoreVanilla && !data.fromPotionCauldron))) {
                return false;
            }

            ItemStack itemStack = data.user.getStackInHand(data.hand);

            PotionEffectInstance eCopy = new PotionEffectInstance(effect);

            float levelDilution = 1.0f / data.entity.getLevel();
            data.entity.addEffect(levelDilution * data.entity.getEffectNerf(effect.type), eCopy);

            itemUse(1, data.user, data.hand, itemStack, null);

            return true;
        };
    }


    private static Boolean potionOnUse(OnUseData data) {
        // full
        if (data.entity.getLevel() >= PotionCauldronBlock.MAX_LEVEL) {
            return false;
        }

        ItemStack itemStack = data.user.getStackInHand(data.hand);

        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);

        // config disallowing mixing of separate potions (water will always be mixable)
        if (!Config.allowMixing && !effects.isEmpty() && !data.entity.getEffects().isEmpty()) {
            if (data.entity.getEffects().size() > 1 || effects.size() > 1) {
                return false;
            }

            if (data.entity.getEffects().get(0).getEffectType() != effects.get(0).getEffectType()) {
                return false;
            }
        }

        if (!data.entity.addLevel(effects)) {
            return false;
        }

        itemUse(1, data.user, data.hand, itemStack, new ItemStack(Items.GLASS_BOTTLE));

        return true;
    }

    private static Boolean bottleOnUse(OnUseData data) {
        if (!data.fromPotionCauldron) {
            return false;
        }

        ItemStack bottles = data.user.getStackInHand(data.hand);

        ItemStack potion = data.entity.pickupFluid();

        itemUse(1, data.user, data.hand, bottles, potion);

        // empty so force it to become cauldron
        if (data.entity.getLevel() < PotionCauldronBlock.MIN_LEVEL) {
            BlockState cauldron = Blocks.CAULDRON.getDefaultState();
            data.world.setBlockState(data.pos, cauldron);
        }

        return true;
    }

    private static Boolean milkOnUse(OnUseData data) {
        if (!data.fromPotionCauldron) {
            return false;
        }

        ItemStack milk = data.user.getStackInHand(data.hand);

        itemUse(1, data.user, data.hand, milk, new ItemStack(Items.BUCKET));

        int level = data.entity.getLevel();

        if (level > 0) {
            BlockState cauldron = Blocks.WATER_CAULDRON.getDefaultState();
            data.world.setBlockState(data.pos, cauldron.with(LeveledCauldronBlock.LEVEL, level));
        }
        else {
            BlockState cauldron = Blocks.CAULDRON.getDefaultState();
            data.world.setBlockState(data.pos, cauldron);
        }

        return true;
    }
}
