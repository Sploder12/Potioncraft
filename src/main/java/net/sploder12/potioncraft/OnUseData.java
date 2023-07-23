package net.sploder12.potioncraft;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
                   this is technically divergent from Vanilla,
                   in creative will not give you a new item
                   if it already exists in the inventory.
                   I'm not doing this cause it's slow and nobody will notice.
                */

                player.getInventory().insertStack(replace);
            }
        }

        return items;
    }


    private static Boolean potionOnUse(OnUseData data) {
        ItemStack itemStack = data.user.getStackInHand(data.hand);

        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);
        if (effects.isEmpty()) {
            return false;
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
