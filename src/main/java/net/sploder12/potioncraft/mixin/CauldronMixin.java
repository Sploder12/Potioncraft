package net.sploder12.potioncraft.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        ItemStack itemStack = user.getStackInHand(hand);
        Block block = state.getBlock();

        int level = 0;
        if (block instanceof LeveledCauldronBlock) {
            level = state.get(LeveledCauldronBlock.LEVEL);
        }

        if (PotionCauldronBlock.canInteract(itemStack.getItem())) {

            BlockState potionCauldron = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
            PotionCauldronBlockEntity entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, potionCauldron);

            assert entity != null;

            entity.setLevel(level);

            if (PotionCauldronBlock.getInteraction(itemStack.getItem()).apply(
                    new OnUseData(entity, state, world, pos, user, hand, hit, false)
            )) {
                if (entity.getEffects().isEmpty()) { // for water and such
                    return;
                }

                info.setReturnValue(ActionResult.SUCCESS);
                info.cancel();
            }
            else{
                return;
            }

            world.setBlockState(pos, potionCauldron);

            BlockEntity dest = world.getBlockEntity(pos);
            assert dest != null;

            dest.readNbt(entity.createNbt());
        }
    }
}
