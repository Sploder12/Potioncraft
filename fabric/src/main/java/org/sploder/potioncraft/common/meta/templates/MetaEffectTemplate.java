package org.sploder.potioncraft.common.meta.templates;

import net.minecraft.util.Identifier;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.conditional.*;
import org.sploder.potioncraft.common.meta.templates.controlflow.If;
import org.sploder.potioncraft.common.meta.templates.effect.*;

import java.util.HashMap;

public interface MetaEffectTemplate {

    // quickfail is an optional parameter that can be used on any template.
    // { "quickfail":"PASS"/"SUCCESS"/"CONSUME"/"FAIL"/"CONSUME_PARTIAL" }
    // setting a value means if prev == that value, the effect will not occur.
    // having no value means the effect will ALWAYS occur
    // it will never appear in `params`.

    Identifier id();

    MetaEffect apply(String id);

    // all the possible effects a meta file is capable of
    HashMap<String, MetaEffectTemplate> templates = new HashMap<>();

    static void register() {
        templates.clear();

        templates.put("FORCE_SWING_HAND", new ForceSwingHand());
        templates.put("PASS", new Pass());
        templates.put("FORWARD", new Forward());
        templates.put("INVERT_COND", new Invert());

        templates.put("AND", new And());
        templates.put("OR", new Or());
        templates.put("NOT", new Not());

        templates.put("IS_FROM_VANILLA", new IsFromVanilla());
        templates.put("HAS_LEVEL", new HasLevel());
        templates.put("HAS_HEAT", new HasHeat());
        templates.put("IS_FULL", new IsFull());

        templates.put("MIN_LEVEL", new MinLevel());
        templates.put("MAX_LEVEL", new MaxLevel());
        templates.put("MIN_HEAT", new MinHeat());
        templates.put("MAX_HEAT", new MaxHeat());

        templates.put("HAS_FLUID", new HasFluid());

        templates.put("ITEM_HAS_EFFECTS", new ItemHasEffects());

        templates.put("USE_ITEM", new UseItem());
        templates.put("PLAY_SOUND", new PlaySound());

        templates.put("CLEAR_EFFECTS", new ClearEffects());
        templates.put("INVERT_EFFECTS", new InvertEffects());
        templates.put("ADD_STATUS_EFFECT", new AddStatusEffect());
        templates.put("ADD_POTION_EFFECT", new AddPotionEffect());
        templates.put("APPLY_ITEM_EFFECTS", new ApplyItemEffects());

        templates.put("ADD_LEVEL", new AddLevel());
        templates.put("REMOVE_LEVEL", new RemoveLevel());
        templates.put("SET_FLUID", new SetFluid());

        templates.put("AMPLIFY", new Amplify());
        templates.put("EXTEND", new Extend());

        templates.put("IF", new If());
    }
}
