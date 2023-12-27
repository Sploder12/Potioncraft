package net.sploder12.potioncraft.meta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.OnUseData;
import net.sploder12.potioncraft.OnUseData.BlockData;
import net.sploder12.potioncraft.PotionCauldronBlock;
import net.sploder12.potioncraft.PotionEffectInstance;

import java.util.List;
import java.util.Optional;

import static net.sploder12.potioncraft.PotionCauldronBlockEntity.CRAFTED_POTION;

public interface MetaEffectTemplate {
    // you may assume params is a JsonObject

    private static JsonPrimitive getPrim(JsonElement elem) {
        if (elem == null || !elem.isJsonPrimitive()) {
            return null;
        }

        return elem.getAsJsonPrimitive();
    }

    public static Identifier getId(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isString()) {
            return null;
        }

        return Identifier.tryParse(prim.getAsString());
    }

    public static Number getNumber(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isNumber()) {
            return null;
        }

        return prim.getAsNumber();
    }

    public static Boolean getBool(JsonElement elem) {
        JsonPrimitive prim = getPrim(elem);
        if (prim == null || !prim.isBoolean()) {
            return null;
        }

        return prim.getAsBoolean();
    }

    public static boolean getBoolOr(JsonElement elem, boolean def) {
        Boolean b = getBool(elem);

        if (b == null) {
            return def;
        }

        return b;
    }


    // quickfail is an optional parameter that can be used on any* template.
    // { "quickfail":"PASS"/"SUCCESS"/"CONSUME"/"FAIL"/"CONSUME_PARTIAL" }
    // setting a value means if prev == that value, the effect will not occur.
    // having no value means the effect will ALWAYS occur
    // *this holds for all templates provided by potioncraft, no guarantees otherwise.

    MetaEffect apply(Optional<ActionResult> quickfail, JsonObject params);

    // hand swinging is controlled by the LAST event, thus why FORCE_SWING_HAND exists.
    // FORCE_SWING_HAND can also be used to generate a guaranteed SUCCESS
    // (or PASS if you use two FORCE_SWING_HANDs with a quickfail == SUCCESS)

    MetaEffectTemplate FORCE_SWING_HAND = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            return ActionResult.success(world.isClient);
        };
    };

    // Predicate templates are useful for checking conditions!
    // && is easy! || is harder but doable via De Morgan's law

    MetaEffectTemplate INVERT_COND = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (prev == ActionResult.PASS) {
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        };
    };

    MetaEffectTemplate IS_FROM_VANILLA = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (data.vanilla) {
                return ActionResult.success(world.isClient);
            }
            else {
                return ActionResult.PASS;
            }
        };
    };

    // params: "level": int - if set will only succeed when data.level == int
    // else will succeed when data.level > 0
    MetaEffectTemplate HAS_LEVEL = (quickfail, params) -> {
        Number num = getNumber(params.get("level"));
        final Integer finalTarget = num == null ? null : num.intValue();

        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (finalTarget == null) {
                if (data.level > 0) {
                    return ActionResult.success(world.isClient);
                }

                return ActionResult.PASS;
            }

            if (finalTarget == data.level) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    MetaEffectTemplate IS_FULL = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (data.level >= PotionCauldronBlock.MAX_LEVEL) {
                return ActionResult.success(world.isClient);
            }

            return ActionResult.PASS;
        };
    };

    MetaEffectTemplate ITEM_HAS_EFFECTS = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (PotionUtil.getPotionEffects(stack).isEmpty()) {
                return ActionResult.PASS;
            }

            return ActionResult.success(world.isClient);
        };
    };

    // params: "id": Identifier - item to replace with
    // "applyPotion": Boolean - adds the potion effects to the replacement item
    MetaEffectTemplate USE_ITEM = (quickfail, params) -> {
        Identifier id = getId(params.get("id"));
        Item replaceItem = null;
        if (id != null) {
            replaceItem = Registries.ITEM.get(id);
        }

        Identifier sid = getId(params.get("sound"));
        SoundEvent sound = null;
        if (id != null) {
            sound = Registries.SOUND_EVENT.get(sid);
        }

        final Item finalReplaceItem = replaceItem;
        final SoundEvent finalSound = sound;
        final boolean finalApplyPotion = getBoolOr(params.get("applyPotion"), false);
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (stack.isEmpty()) {
                return ActionResult.PASS;
            }


            ItemStack out = ItemStack.EMPTY;
            if (finalReplaceItem != null) {
                out = new ItemStack(finalReplaceItem);

                if (finalApplyPotion) {
                    if (finalReplaceItem == Items.POTION) {
                        if (!data.entity.hasEffects()) {
                            PotionUtil.setPotion(out, Potions.WATER);
                        }
                        else {
                            PotionUtil.setPotion(out, CRAFTED_POTION);
                        }
                    }

                    data.entity.setEffects(out);
                }
            }

            OnUseData.itemUse(hand, stack, player, out);

            if (finalSound != null && !world.isClient) {
                world.playSound(null, pos, finalSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResult.success(world.isClient);
        };
    };

    // params: "id": Identifier - sound to play
    MetaEffectTemplate PLAY_SOUND = (quickfail, params) -> {
        Identifier id = getId(params.get("id"));
        if (id != null) {
            final SoundEvent sound = Registries.SOUND_EVENT.get(id);
            if (sound != null) {
                return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                    if (quickfail.isPresent() && quickfail.get() == prev) {
                        return ActionResult.PASS;
                    }

                    if (!world.isClient) {
                        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }

                    return ActionResult.success(world.isClient);
                };
            }
        }

        Main.log("PLAY_SOUND effect has invalid id field!");
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
    };

    // think milk bucket
    MetaEffectTemplate CLEAR_EFFECTS = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            data.entity.clearEffects();

            return ActionResult.success(world.isClient);
        };
    };

    // think spider eye
    MetaEffectTemplate INVERT_EFFECTS = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            data.entity.invertEffects();

            return ActionResult.success(world.isClient);
        };
    };

    // params: "id": Identifier - status effect to add
    // "duration": Decimal - duration
    // "amplifier": Decimal - effect level
    // "showParticles": Boolean - should show particles?
    // "showIcon": Boolean - should show icon?
    MetaEffectTemplate ADD_STATUS_EFFECT = (quickfail, params) -> {
        Identifier id = getId(params.get("id"));
        if (id != null) {
            StatusEffect type = Registries.STATUS_EFFECT.get(id);
            if (type != null) {
                Number dur = getNumber(params.get("duration"));
                final float duration = dur == null ? 1.0f : dur.floatValue();

                Number amp = getNumber(params.get("amplifier"));
                final float amplifier = amp == null ? 0.0f : amp.floatValue();

                final boolean showParticles = getBoolOr(params.get("showParticles"), true);
                final boolean showIcon = getBoolOr(params.get("showIcon"), true);

                return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                    if (quickfail.isPresent() && quickfail.get() == prev) {
                        return ActionResult.PASS;
                    }

                    if (data.level == 0) {
                        return ActionResult.PASS;
                    }

                    PotionEffectInstance effect = new PotionEffectInstance(type, duration, amplifier, false, showParticles, showIcon);

                    float dilution = 1.0f / data.level;
                    data.entity.addEffect(dilution, effect);


                    return ActionResult.success(world.isClient);
                };
            }
        }

        Main.log("ADD_POTION_EFFECT effect has invalid id field!");
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
    };

    // params: "id": Identifier - potion effect to add
    // WARNING - ONLY works when the potion has a single effect.
    MetaEffectTemplate ADD_POTION_EFFECT = (quickfail, params) -> {
        Identifier id = getId(params.get("id"));
        if (id != null) {
            Potion potion = Registries.POTION.get(id);
            if (potion != Potions.EMPTY) {
                return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
                    if (quickfail.isPresent() && quickfail.get() == prev) {
                        return ActionResult.PASS;
                    }

                    if (data.level == 0) {
                        return ActionResult.PASS;
                    }

                    PotionEffectInstance effect = new PotionEffectInstance(potion);

                    float dilution = 1.0f / data.level;
                    data.entity.addEffect(dilution, effect);

                    return ActionResult.success(world.isClient);
                };
            }
        }

        Main.log("ADD_POTION_EFFECT effect has invalid id field!");
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> ActionResult.PASS;
    };

    // takes the potion effects from item and applies to the cauldron
    MetaEffectTemplate APPLY_ITEM_EFFECTS = (quickfail, params) -> {
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (data.level == 0) {
                return ActionResult.PASS;
            }

            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
            if (!effects.isEmpty()) {
                data.entity.addEffects(effects);
            }

            return ActionResult.success(world.isClient);
        };
    };

    // params: "dilute": boolean - should dilution occur
    MetaEffectTemplate ADD_LEVEL = (quickfail, params) -> {
        final boolean dilute = getBoolOr(params.get("dilute"), true);
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (data.level >= PotionCauldronBlock.MAX_LEVEL) {
                return ActionResult.PASS;
            }

            data.entity.addLevel(dilute);
            data.level += 1;

            return ActionResult.success(world.isClient);
        };
    };

    // removes a water level from the cauldron
    MetaEffectTemplate REMOVE_LEVEL = (quickfail, params) -> {
        final boolean dilute = getBoolOr(params.get("dilute"), true);
        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            if (data.level < PotionCauldronBlock.MIN_LEVEL) {
                return ActionResult.PASS;
            }

            data.entity.removeLevel();
            data.level -= 1;

            return ActionResult.success(world.isClient);
        };
    };

    // amplifies the effect level (evenly adds "amplifier" to all effects)
    // params: "amplifier" - Decimal
    MetaEffectTemplate AMPLIFY = (quickfail, params) -> {
        Number amp = getNumber(params.get("amplifier"));
        final float amplifier = amp == null ? 3.0f : amp.floatValue();

        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            data.entity.amplify(amplifier);

            return ActionResult.success(world.isClient);
        };
    };

    // extends the effect (evenly adds "duration" to all effects)
    // params: "duration" - Decimal
    MetaEffectTemplate EXTEND = (quickfail, params) -> {
        Number dur = getNumber(params.get("duration"));
        final float duration = dur == null ? 6000.0f : dur.floatValue();

        return (ActionResult prev, BlockData data, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) -> {
            if (quickfail.isPresent() && quickfail.get() == prev) {
                return ActionResult.PASS;
            }

            data.entity.extendDuration(duration);

            return ActionResult.success(world.isClient);
        };
    };

    static void register() {
        MetaMixing.templates.put("FORCE_SWING_HAND", FORCE_SWING_HAND);
        MetaMixing.templates.put("INVERT_COND", INVERT_COND);

        MetaMixing.templates.put("IS_FROM_VANILLA", IS_FROM_VANILLA);
        MetaMixing.templates.put("HAS_LEVEL", HAS_LEVEL);
        MetaMixing.templates.put("IS_FULL", IS_FULL);
        MetaMixing.templates.put("ITEM_HAS_EFFECTS", ITEM_HAS_EFFECTS);

        MetaMixing.templates.put("USE_ITEM", USE_ITEM);
        MetaMixing.templates.put("PLAY_SOUND", PLAY_SOUND);

        MetaMixing.templates.put("CLEAR_EFFECTS", CLEAR_EFFECTS);
        MetaMixing.templates.put("INVERT_EFFECTS", INVERT_EFFECTS);
        MetaMixing.templates.put("ADD_STATUS_EFFECT", ADD_STATUS_EFFECT);
        MetaMixing.templates.put("ADD_POTION_EFFECT", ADD_POTION_EFFECT);
        MetaMixing.templates.put("APPLY_ITEM_EFFECTS", APPLY_ITEM_EFFECTS);

        MetaMixing.templates.put("ADD_LEVEL", ADD_LEVEL);
        MetaMixing.templates.put("REMOVE_LEVEL", REMOVE_LEVEL);

        MetaMixing.templates.put("AMPLIFY", AMPLIFY);
        MetaMixing.templates.put("EXTEND", EXTEND);
    }
}
