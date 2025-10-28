package org.sploder.potioncraft.common.meta.templates.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

// takes the potion effects from item and applies to the cauldron
public class ApplyItemEffects implements MetaEffectTemplate {
    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/APPLY_ITEM_EFFECTS");
    }

    @Override
    public MetaEffect apply(String id) {
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (data.getLevel() == 0) {
                return ActionResult.PASS;
            }

            var effects = PotionUtil.getPotionEffects(stack);
            if (!effects.isEmpty()) {
                data.entity.addEffects(effects);
            }

            return ActionResult.success(world.isClient);
        };
    }
}
