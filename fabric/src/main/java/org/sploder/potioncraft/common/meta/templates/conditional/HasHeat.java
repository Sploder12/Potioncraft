package org.sploder.potioncraft.common.meta.templates.conditional;

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

public class HasHeat implements MetaEffectTemplate {
    // if set will only succeed when data.heat >= int
    // or == when int is 0, or <= when int is < 0
    // default is as if the parameter was 1
    @Argument(key="heat", optional = true)
    int target = 1;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/HAS_LEVEL");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalTarget = target;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            // target heat is positive
            if (finalTarget > 0) {
                if (data.heat >= finalTarget) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            }

            // target heat is negative
            if (finalTarget < 0) {
                if (data.heat <= finalTarget) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            }

            // target heat is 0
            if (finalTarget == data.heat) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    }
}