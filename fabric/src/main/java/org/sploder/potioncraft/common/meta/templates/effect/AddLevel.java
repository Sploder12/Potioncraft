package org.sploder.potioncraft.common.meta.templates.effect;

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
import org.sploder.potioncraft.common.PotionCauldronBlock;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

public class AddLevel extends MetaEffectTemplate {
    // potion effect to add
    @Argument(key = "dilute", optional = true)
    boolean dilute = true;

    @Argument(key = "fluid", optional = true)
    Fluid fluid = Fluids.EMPTY;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/add_level");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalDilute = dilute;
        if (fluid != Fluids.EMPTY) {
            final var finalFluid = fluid;
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (data.getLevel() >= PotionCauldronBlock.MAX_LEVEL) {
                    return ActionResult.PASS;
                }

                if (data.getFluid() != Fluids.EMPTY && data.getFluid() != finalFluid) {
                    return ActionResult.PASS;
                }

                data.setFluid(finalFluid);
                data.addLevel(finalDilute);

                return ActionResult.success(world.isClient);
            };
        }

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (!data.addLevel(finalDilute)) {
                return ActionResult.PASS;
            }

            return ActionResult.success(world.isClient);
        };
    }
}
