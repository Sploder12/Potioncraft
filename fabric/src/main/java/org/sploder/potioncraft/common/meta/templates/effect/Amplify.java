package org.sploder.potioncraft.common.meta.templates.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

// amplifies the effect level (evenly adds "amplifier" to all effects)
public class Amplify extends MetaEffectTemplate {
    // potion effect to add
    @Argument(key = "amplifier", optional = true)
    float amplifier = 3.0f;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/amplify");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalAmplifier = amplifier;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            data.entity.amplify(finalAmplifier);

            return ActionResult.success(world.isClient);
        };
    }
}
