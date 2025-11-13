package org.sploder.potioncraft.common.meta.templates.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.PotionEffectInstance;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;
import org.sploder.potioncraft.common.meta.templates.conditional.Pass;

// WARNING - ONLY works when the potion has a single effect.
public class AddPotionEffect extends MetaEffectTemplate {
    // potion effect to add
    @Argument(key = "id")
    Potion potion;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/add_potion_effect");
    }

    @Override
    public MetaEffect apply(String id) {
        if (potion == Potions.EMPTY) {
            Log.warn("ADD_POTION_EFFECT is using an empty potion! " + id);
            return new Pass().apply(id);
        }

        final var finalPotion = potion;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.getLevel() == 0) {
                return ActionResult.PASS;
            }

            PotionEffectInstance effect = new PotionEffectInstance(finalPotion);

            float dilution = 1.0f / data.getLevel();
            data.entity.addEffect(dilution, effect);

            return ActionResult.success(world.isClient);
        };
    }
}
