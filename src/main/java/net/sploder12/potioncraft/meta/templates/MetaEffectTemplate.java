package net.sploder12.potioncraft.meta.templates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import net.sploder12.potioncraft.PotionEffectInstance;
import net.sploder12.potioncraft.meta.CauldronData;
import net.sploder12.potioncraft.meta.MetaEffect;
import net.sploder12.potioncraft.meta.MetaMixing;
import net.sploder12.potioncraft.util.Json;

import java.util.HashSet;
import java.util.List;

import static net.sploder12.potioncraft.PotionCauldronBlockEntity.CRAFTED_POTION;

public interface MetaEffectTemplate {

    // quickfail is an optional parameter that can be used on any template.
    // { "quickfail":"PASS"/"SUCCESS"/"CONSUME"/"FAIL"/"CONSUME_PARTIAL" }
    // setting a value means if prev == that value, the effect will not occur.
    // having no value means the effect will ALWAYS occur

    MetaEffect apply(JsonObject params);

    static void register() {
        MetaMixing.templates.put("FORCE_SWING_HAND", Conditional.FORCE_SWING_HAND);
        MetaMixing.templates.put("INVERT_COND", Conditional.INVERT_COND);

        MetaMixing.templates.put("IS_FROM_VANILLA", Conditional.IS_FROM_VANILLA);
        MetaMixing.templates.put("HAS_LEVEL", Conditional.HAS_LEVEL);
        MetaMixing.templates.put("HAS_HEAT", Conditional.HAS_HEAT);
        MetaMixing.templates.put("IS_FULL", Conditional.IS_FULL);

        MetaMixing.templates.put("MIN_LEVEL", Conditional.MIN_LEVEL);
        MetaMixing.templates.put("MAX_LEVEL", Conditional.MAX_LEVEL);
        MetaMixing.templates.put("MIN_HEAT", Conditional.MIN_HEAT);
        MetaMixing.templates.put("MAX_HEAT", Conditional.MAX_HEAT);

        MetaMixing.templates.put("HAS_FLUID", Conditional.HAS_FLUID);

        MetaMixing.templates.put("ITEM_HAS_EFFECTS", Conditional.ITEM_HAS_EFFECTS);

        MetaMixing.templates.put("USE_ITEM", Effect.USE_ITEM);
        MetaMixing.templates.put("PLAY_SOUND", Effect.PLAY_SOUND);

        MetaMixing.templates.put("CLEAR_EFFECTS", Effect.CLEAR_EFFECTS);
        MetaMixing.templates.put("INVERT_EFFECTS", Effect.INVERT_EFFECTS);
        MetaMixing.templates.put("ADD_STATUS_EFFECT", Effect.ADD_STATUS_EFFECT);
        MetaMixing.templates.put("ADD_POTION_EFFECT", Effect.ADD_POTION_EFFECT);
        MetaMixing.templates.put("APPLY_ITEM_EFFECTS", Effect.APPLY_ITEM_EFFECTS);

        MetaMixing.templates.put("ADD_LEVEL", Effect.ADD_LEVEL);
        MetaMixing.templates.put("REMOVE_LEVEL", Effect.REMOVE_LEVEL);

        MetaMixing.templates.put("AMPLIFY", Effect.AMPLIFY);
        MetaMixing.templates.put("EXTEND", Effect.EXTEND);
    }
}
