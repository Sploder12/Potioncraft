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

public class MaxHeat extends MetaEffectTemplate {
    // will only succeed when data.heat <= int
    @Argument(key="heat")
    int target;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/max_heat");
    }

    @Override
    public MetaEffect apply(String id) {
        final int finalTarget = target;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.heat <= finalTarget) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    }
}
