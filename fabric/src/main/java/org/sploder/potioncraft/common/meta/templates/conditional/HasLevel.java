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

public class HasLevel extends MetaEffectTemplate {
    // if set will only succeed when data.getLevel() == int
    // else will succeed when data.getLevel() > 0
    @Argument(key="level", optional = true)
    Integer target = null;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/has_level");
    }

    @Override
    public MetaEffect apply(String id) {
        if (target == null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (data.getLevel() > 0) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            };
        }

        final var finalTarget = target;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (finalTarget == data.getLevel()) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    }
}
