package net.sploder12.potioncraft.mixin;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PotionEntity.class)
abstract class LingeringMixin {

    @Redirect(method = "applyLingeringPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/AreaEffectCloudEntity;addEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"))
    private void LingeringDurationFix(AreaEffectCloudEntity areaEffectCloudEntity, StatusEffectInstance effect) {
        // i could modify the effect, but the fields are private and it's too much work to modify

        areaEffectCloudEntity.addEffect(
            new StatusEffectInstance(
                effect.getEffectType(),
                Math.max(effect.mapDuration((i) -> i / 4), 1),
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.shouldShowParticles(),
                effect.shouldShowIcon()
        ));

        // note: hidden effects get removed, however, the game already does this and nobody cares.
    }
}
