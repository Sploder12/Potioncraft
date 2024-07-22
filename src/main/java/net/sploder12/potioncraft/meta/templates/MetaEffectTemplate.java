package net.sploder12.potioncraft.meta.templates;

import com.google.gson.JsonObject;
import net.sploder12.potioncraft.meta.MetaEffect;

import java.util.HashMap;

public interface MetaEffectTemplate {

    // quickfail is an optional parameter that can be used on any template.
    // { "quickfail":"PASS"/"SUCCESS"/"CONSUME"/"FAIL"/"CONSUME_PARTIAL" }
    // setting a value means if prev == that value, the effect will not occur.
    // having no value means the effect will ALWAYS occur

    MetaEffect apply(JsonObject params, String file);

    // all the possible effects a meta file is capable of
    HashMap<String, MetaEffectTemplate> templates = new HashMap<>();

    static void register() {
        templates.clear();

        templates.put("FORCE_SWING_HAND", Conditional.FORCE_SWING_HAND);
        templates.put("INVERT_COND", Conditional.INVERT_COND);

        templates.put("AND", Conditional.AND);
        templates.put("OR", Conditional.OR);

        templates.put("IS_FROM_VANILLA", Conditional.IS_FROM_VANILLA);
        templates.put("HAS_LEVEL", Conditional.HAS_LEVEL);
        templates.put("HAS_HEAT", Conditional.HAS_HEAT);
        templates.put("IS_FULL", Conditional.IS_FULL);

        templates.put("MIN_LEVEL", Conditional.MIN_LEVEL);
        templates.put("MAX_LEVEL", Conditional.MAX_LEVEL);
        templates.put("MIN_HEAT", Conditional.MIN_HEAT);
        templates.put("MAX_HEAT", Conditional.MAX_HEAT);

        templates.put("HAS_FLUID", Conditional.HAS_FLUID);

        templates.put("ITEM_HAS_EFFECTS", Conditional.ITEM_HAS_EFFECTS);

        templates.put("USE_ITEM", Effect.USE_ITEM);
        templates.put("PLAY_SOUND", Effect.PLAY_SOUND);

        templates.put("CLEAR_EFFECTS", Effect.CLEAR_EFFECTS);
        templates.put("INVERT_EFFECTS", Effect.INVERT_EFFECTS);
        templates.put("ADD_STATUS_EFFECT", Effect.ADD_STATUS_EFFECT);
        templates.put("ADD_POTION_EFFECT", Effect.ADD_POTION_EFFECT);
        templates.put("APPLY_ITEM_EFFECTS", Effect.APPLY_ITEM_EFFECTS);

        templates.put("ADD_LEVEL", Effect.ADD_LEVEL);
        templates.put("REMOVE_LEVEL", Effect.REMOVE_LEVEL);

        templates.put("AMPLIFY", Effect.AMPLIFY);
        templates.put("EXTEND", Effect.EXTEND);
    }
}
