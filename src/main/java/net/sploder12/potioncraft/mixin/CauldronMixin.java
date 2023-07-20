package net.sploder12.potioncraft.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


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

        if (itemStack.getItem() == Items.POTION) {

            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(itemStack);
            if (effects.isEmpty()) {
                return;
            }

            int level = 0;
            if (block instanceof LeveledCauldronBlock) {
                level = state.get(LeveledCauldronBlock.LEVEL);
                if (level >= PotionCauldronBlock.MAX_LEVEL) {
                    return;
                }
            }

            BlockState potionCauldron = PotionCauldronBlock.POTION_CAULDRON_BLOCK.getDefaultState();
            PotionCauldronBlockEntity entity = (PotionCauldronBlockEntity) PotionCauldronBlock.POTION_CAULDRON_BLOCK.createBlockEntity(pos, potionCauldron);

            assert entity != null;

            entity.setLevel(level);
            entity.addLevel(effects);

            world.setBlockState(pos, potionCauldron);

            BlockEntity dest = world.getBlockEntity(pos);
            assert dest != null;
            dest.readNbt(entity.createNbt());

            info.setReturnValue(ActionResult.SUCCESS);
            info.cancel();
            return;
        }
    }
}
