package org.sploder.potioncraft.common.meta.templates.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.PotionCauldronBlockEntity;
import org.sploder.potioncraft.common.meta.CauldronData;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.Argument;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

public class UseItem implements MetaEffectTemplate {
    // item to replace with
    @Argument(key = "id", optional = true)
    Item replaceItem = null;

    // sound to play
    @Argument(key = "sound", optional = true)
    SoundEvent sound = null;

    // adds the potion effects to the replacement item
    @Argument(key = "applyPotion", optional = true)
    boolean applyPotion = false;

    // attempt to use and give that many items
    @Argument(key = "count", optional = true)
    int count = 1;

    @Override
    public Identifier id() {
        return new Identifier(Common.namespace, "effect/USE_ITEM");
    }

    @Override
    public MetaEffect apply(String id) {
        final var finalReplaceItem = replaceItem;
        final var finalSound = sound;
        final var finalApplyPotion = applyPotion;
        final var finalCount = count;
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (stack.isEmpty()) {
                return ActionResult.PASS;
            }

            int trueCount = Math.min(finalCount, stack.getCount());

            ItemStack out = ItemStack.EMPTY;
            if (finalReplaceItem != null) {
                out = new ItemStack(finalReplaceItem);

                out.setCount(trueCount);

                if (finalApplyPotion) {
                    if (!data.entity.hasEffects()) {
                        PotionUtil.setPotion(out, Potions.WATER);
                    }
                    else {
                        PotionUtil.setPotion(out, PotionCauldronBlockEntity.CRAFTED_POTION);
                    }

                    data.entity.setEffects(out);
                }
            }

            CauldronData.itemUse(world, pos, hand, stack, player, out, trueCount);

            if (finalSound != null && !world.isClient) {
                world.playSound(null, pos, finalSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResult.success(world.isClient);
        };
    }
}
