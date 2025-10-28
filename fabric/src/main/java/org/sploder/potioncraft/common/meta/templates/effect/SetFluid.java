package org.sploder.potioncraft.common.meta.templates.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
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

// sets the fluid contained by the cauldron
public class SetFluid implements MetaEffectTemplate {
    // fluid to set
    @Argument(key = "id")
    Fluid fluid;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/SET_FLUID");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalFluid = fluid;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            data.entity.setFluid(finalFluid);

            return ActionResult.success(world.isClient);
        };
    }
}
