package net.sploder12.potioncraft.meta.templates;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionCauldronBlockEntity;
import net.sploder12.potioncraft.PotionEffectInstance;
import net.sploder12.potioncraft.meta.CauldronData;
import net.sploder12.potioncraft.util.Json;

import java.util.List;

public interface Effect {

    // params: "id": Identifier - item to replace with
    // "applyPotion": Boolean - adds the potion effects to the replacement item
    // "sound": Identifier - sound to play
    // "count": attempt to use and give that many items
    MetaEffectTemplate USE_ITEM = (params, file) -> {

        final Item replaceItem = Json.getRegistryEntry(params.get("id"), Registries.ITEM);
        final SoundEvent sound = Json.getRegistryEntry(params.get("sound"), Registries.SOUND_EVENT);

        final boolean finalApplyPotion = Json.getBoolOr(params.get("applyPotion"), false);
        final int finalCount = Json.getIntOr(params.get("count"), 1);

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (stack.isEmpty()) {
                return ActionResult.PASS;
            }

            int trueCount = Math.min(finalCount, stack.getCount());

            ItemStack out = ItemStack.EMPTY;
            if (replaceItem != null) {
                out = new ItemStack(replaceItem);

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

            if (sound != null && !world.isClient) {
                world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResult.success(world.isClient);
        };
    };

    // params: "id": Identifier - sound to play
    MetaEffectTemplate PLAY_SOUND = (params, file) -> {

        final SoundEvent sound = Json.getRegistryEntry(params.get("id"), Registries.SOUND_EVENT);
        if (sound != null) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (!world.isClient) {
                    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                return ActionResult.success(world.isClient);
            };
        }

        Main.log("WARNING: PLAY_SOUND effect has invalid id field! " + file);
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
    };

    // think milk bucket
    MetaEffectTemplate CLEAR_EFFECTS = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        data.entity.clearEffects();

        return ActionResult.success(world.isClient);
    };

    // think spider eye
    MetaEffectTemplate INVERT_EFFECTS = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        data.entity.invertEffects();

        return ActionResult.success(world.isClient);
    };

    // params: "id": Identifier - status effect to add
    // "duration": Decimal - duration
    // "amplifier": Decimal - effect level
    // "showParticles": Boolean - should show particles?
    // "showIcon": Boolean - should show icon?
    MetaEffectTemplate ADD_STATUS_EFFECT = (params, file) -> {
        final StatusEffect type = Json.getRegistryEntry(params.get("id"), Registries.STATUS_EFFECT);

        if (type != null) {
            final float duration = Json.getFloatOr(params.get("duration"), 1.0f);
            final float amplifier = Json.getFloatOr(params.get("amplifier"), 0.0f);

            final boolean showParticles = Json.getBoolOr(params.get("showParticles"), true);
            final boolean showIcon = Json.getBoolOr(params.get("showIcon"), true);

            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (data.getLevel() == 0) {
                    return ActionResult.PASS;
                }

                PotionEffectInstance effect = new PotionEffectInstance(type, duration, amplifier, false, showParticles, showIcon);

                float dilution = 1.0f / data.getLevel();
                data.entity.addEffect(dilution, effect);


                return ActionResult.success(world.isClient);
            };
        }

        Main.log("WARNING: ADD_POTION_EFFECT effect has invalid id field! " + file);
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
    };

    // params: "id": Identifier - potion effect to add
    // WARNING - ONLY works when the potion has a single effect.
    MetaEffectTemplate ADD_POTION_EFFECT = (params, file) -> {
        final Potion potion = Json.getRegistryEntry(params.get("id"), Registries.POTION);

        if (potion != null && potion != Potions.EMPTY) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (data.getLevel() == 0) {
                    return ActionResult.PASS;
                }

                PotionEffectInstance effect = new PotionEffectInstance(potion);

                float dilution = 1.0f / data.getLevel();
                data.entity.addEffect(dilution, effect);

                return ActionResult.success(world.isClient);
            };
        }

        Main.log("WARNING: ADD_POTION_EFFECT effect has invalid id field! " + file);
        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
    };

    // takes the potion effects from item and applies to the cauldron
    MetaEffectTemplate APPLY_ITEM_EFFECTS = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (data.getLevel() == 0) {
            return ActionResult.PASS;
        }

        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
        if (!effects.isEmpty()) {
            data.entity.addEffects(effects);
        }

        return ActionResult.success(world.isClient);
    };

    // params: "dilute": boolean - should dilution occur
    // "fluid": Identifier - fluid to try to add
    MetaEffectTemplate ADD_LEVEL = (params, file) -> {
        final boolean dilute = Json.getBoolOr(params.get("dilute"), true);

        final Fluid fluid = Json.getRegistryEntry(params.get("fluid"), Registries.FLUID);

        if (fluid != null && fluid != Fluids.EMPTY) {
            return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                if (data.getLevel() >= PotionCauldronBlock.MAX_LEVEL) {
                    return ActionResult.PASS;
                }

                if (data.getFluid() != Fluids.EMPTY && data.getFluid() != fluid) {
                    return ActionResult.PASS;
                }

                data.setFluid(fluid);
                data.addLevel(dilute);

                return ActionResult.success(world.isClient);
            };
        }

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (!data.addLevel(dilute)) {
                return ActionResult.PASS;
            }

            return ActionResult.success(world.isClient);
        };
    };

    // removes a water level from the cauldron
    MetaEffectTemplate REMOVE_LEVEL = (params, file) -> (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
        if (!data.removeLevel()) {
            return ActionResult.PASS;
        }

        return ActionResult.success(world.isClient);
    };

    // amplifies the effect level (evenly adds "amplifier" to all effects)
    // params: "amplifier" - Decimal
    MetaEffectTemplate AMPLIFY = (params, file) -> {
        final float amplifier = Json.getFloatOr(params.get("amplifier"), 3.0f);

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            data.entity.amplify(amplifier);

            return ActionResult.success(world.isClient);
        };
    };

    // extends the effect (evenly adds "duration" to all effects)
    // params: "duration" - Decimal
    MetaEffectTemplate EXTEND = (params, file) -> {
        final float duration = Json.getFloatOr(params.get("duration"), 6000.0f);

        return (ActionResult prev, CauldronData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            data.entity.extendDuration(duration);

            return ActionResult.success(world.isClient);
        };
    };
}
