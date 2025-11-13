package org.sploder.potioncraft.common.meta.templates.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.PotionEffectInstance;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

public class AddStatusEffect extends MetaEffectTemplate {
    // potion effect to add
    @Argument(key = "id")
    StatusEffect type;

    @Argument(key = "duration", optional = true)
    float duration = 1.0f;

    @Argument(key = "amplifier", optional = true)
    float amplifier = 0.0f;

    @Argument(key = "showParticles", optional = true)
    boolean showParticles = true;

    @Argument(key = "showIcon", optional = true)
    boolean showIcon = true;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/add_status_effect");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalType = type;
        final var finalDuration = duration;
        final var finalAmplifier = amplifier;
        final var finalShowParticles = showParticles;
        final var finalShowIcon = showIcon;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.getLevel() == 0) {
                return ActionResult.PASS;
            }

            PotionEffectInstance effect = new PotionEffectInstance(finalType, finalDuration, finalAmplifier, false, finalShowParticles, finalShowIcon);

            float dilution = 1.0f / data.getLevel();
            data.entity.addEffect(dilution, effect);

            return ActionResult.success(world.isClient);
        };
    }
}
