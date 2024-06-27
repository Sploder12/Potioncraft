package net.sploder12.potioncraft.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArrowEntity.class)
abstract class TippedArrowMixin {

    @Redirect(method = "onHit(Lnet/minecraft/entity/LivingEntity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"))
    private boolean TippedDurationFix(LivingEntity target, StatusEffectInstance effect, Entity entity) {
        return target.addStatusEffect(
            new StatusEffectInstance(
                effect.getEffectType(),
                Math.max(effect.mapDuration((i) -> i / 8), 1),
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.shouldShowParticles(),
                effect.shouldShowIcon()),
            entity);
    }
}
