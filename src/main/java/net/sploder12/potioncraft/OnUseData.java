package net.sploder12.potioncraft;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
        PotionCauldronBlock.addInteraction(Items.MILK_BUCKET, OnUseData::milkOnUse);
    }

    private static Boolean milkOnUse(OnUseData data) {
        if (!data.fromPotionCauldron) {
            return false;
        }

        ItemStack bucket = data.user.getStackInHand(data.hand);

        if (!data.user.isCreative()) {
            bucket.decrement(1);
            if (bucket.isEmpty()) {
                data.user.setStackInHand(data.hand, new ItemStack(Items.BUCKET));
            }
            else {
                data.user.getInventory().insertStack(new ItemStack(Items.BUCKET));
            }
        }

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
