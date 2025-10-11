package net.sploder12.potioncraft.meta;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface MetaEffect {
    // you may assume data.valid is always true
    // prev will always be ActionResult.SUCCESS on the first effect

    ActionResult interact(ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack);
}
