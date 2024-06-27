package net.sploder12.potioncraft.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.BrewingRecipeRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingStandMixin {
    @Inject(method = "craft(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void CustomEffectFix(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        if (input.isEmpty()) {
            cir.cancel();
            return;
        }

        ItemStack outStack = cir.getReturnValue();
        List<StatusEffectInstance> effects = PotionUtil.getCustomPotionEffects(input);

        if (!effects.isEmpty()) {
            cir.setReturnValue(PotionUtil.setCustomPotionEffects(outStack, effects));
        }
    }
}
