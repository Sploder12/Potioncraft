package net.sploder12.potioncraft.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.OnUseData;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.sploder12.potioncraft.OnUseData.interactions;

@Mixin(AbstractCauldronBlock.class)
public abstract class CauldronMixin {

    @Inject(at = @At("HEAD"),
            method = "onUse(" +
                        "Lnet/minecraft/block/BlockState;" +
                        "Lnet/minecraft/world/World;" +
                        "Lnet/minecraft/util/math/BlockPos;" +
                        "Lnet/minecraft/entity/player/PlayerEntity;" +
                        "Lnet/minecraft/util/Hand;" +
                        "Lnet/minecraft/util/hit/BlockHitResult;)" +
                    "Lnet/minecraft/util/ActionResult;",
            cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity user, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> info) {

        Block block = state.getBlock();
        if (block instanceof LeveledCauldronBlock) {
            ItemStack itemStack = user.getStackInHand(hand);

            CauldronBehavior behavior = interactions.get(itemStack.getItem());

            ActionResult res = behavior.interact(state, world, pos, user, hand, itemStack);

            if (res == ActionResult.SUCCESS) {
                info.setReturnValue(ActionResult.SUCCESS);
                info.cancel();
            }
        }
    }
}
