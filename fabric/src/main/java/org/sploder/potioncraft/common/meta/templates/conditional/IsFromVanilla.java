package org.sploder.potioncraft.common.meta.templates.conditional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

public class IsFromVanilla extends MetaEffectTemplate {
    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "conditional/is_from_vanilla");
    }

    @Override
    public MetaEffect apply(String id) {
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (Registries.BLOCK.getId(data.source).getNamespace().equalsIgnoreCase("minecraft")) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    }
}
