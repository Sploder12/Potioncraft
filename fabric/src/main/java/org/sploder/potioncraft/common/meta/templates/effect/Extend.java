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

// extends the effect (evenly adds "duration" to all effects)
public class Extend extends MetaEffectTemplate {
    // potion effect to add
    @Argument(key = "duration", optional = true)
    float duration = 6000.0f;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/extend");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalDuration = duration;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            data.entity.extendDuration(finalDuration);

            return ActionResult.success(world.isClient);
        };
    }
}
