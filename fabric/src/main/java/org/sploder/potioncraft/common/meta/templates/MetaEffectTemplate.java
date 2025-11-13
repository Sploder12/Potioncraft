package org.sploder.potioncraft.common.meta.templates;

import net.minecraft.util.Identifier;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.meta.MetaEffect;
import org.sploder.potioncraft.common.meta.templates.conditional.*;
import org.sploder.potioncraft.common.meta.templates.controlflow.If;
import org.sploder.potioncraft.common.meta.templates.effect.*;

public abstract class MetaEffectTemplate {

    // quickfail is an optional parameter that can be used on any template.
    // { "quickfail":"PASS"/"SUCCESS"/"CONSUME"/"FAIL"/"CONSUME_PARTIAL" }
    // setting a value means if prev == that value, the effect will not occur.
    // having no value means the effect will ALWAYS occur
    // it will never appear in `params`.

    public abstract Identifier id();

    public String shortID() {
        var parts = id().getPath().split("/");
        if (parts.length == 0 || parts[parts.length - 1].isEmpty()) {
            Log.warn(this.getClass() + " has invalid short ID " + id());
            return null;
        }

        return parts[parts.length - 1];
    }

    public abstract MetaEffect apply(String id);

    public static void register(TemplateResolver resolver) {
        resolver.register(new ForceSwingHand());

        resolver.register(new Pass());
        resolver.register(new Forward());
        resolver.register(new Invert());

        resolver.register(new And(resolver));
        resolver.register(new Or(resolver));
        resolver.register(new Not(resolver));

        resolver.register(new IsFromVanilla());
        resolver.register(new HasLevel());
        resolver.register(new HasHeat());
        resolver.register(new IsFull());

        resolver.register(new MinLevel());
        resolver.register(new MaxLevel());
        resolver.register(new MinHeat());
        resolver.register(new MaxHeat());

        resolver.register(new HasFluid());

        resolver.register(new ItemHasEffects());

        resolver.register(new UseItem());
        resolver.register(new PlaySound());

        resolver.register(new ClearEffects());
        resolver.register(new InvertEffects());
        resolver.register(new AddStatusEffect());
        resolver.register(new AddPotionEffect());
        resolver.register(new ApplyItemEffects());

        resolver.register(new AddLevel());
        resolver.register(new RemoveLevel());
        resolver.register(new SetFluid());

        resolver.register(new Amplify());
        resolver.register(new Extend());

        resolver.register(new If(resolver));
    }
}
