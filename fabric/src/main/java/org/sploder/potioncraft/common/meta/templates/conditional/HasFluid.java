package org.sploder.potioncraft.common.meta.templates.conditional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
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

import java.util.Set;

public class HasFluid extends MetaEffectTemplate {
    // array of identifiers to fluids
    @Argument(key="fluids", optional = true)
    Set<Fluid> fluids = null;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/has_fluid");
    }

    @Override
    public MetaEffect apply(String id) {
        if (fluids == null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (data.getFluid() != Fluids.EMPTY) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            };
        }

        final var finalFluids = fluids;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (finalFluids.contains(data.getFluid())) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    }
}
