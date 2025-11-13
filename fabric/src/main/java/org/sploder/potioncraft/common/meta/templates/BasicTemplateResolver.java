package org.sploder.potioncraft.common.meta.templates;

import net.minecraft.util.Identifier;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicTemplateResolver implements TemplateResolver {

    Map<String, List<MetaEffectTemplate>> shortnames = new HashMap<>();
    Map<Identifier, MetaEffectTemplate> fullnames = new HashMap<>();

    @Override
    public <T extends MetaEffectTemplate> void register(T template) {
        var shortname = template.shortID().toLowerCase();
        var list = shortnames.computeIfAbsent(shortname, k -> new ArrayList<>());
        list.add(template);

        if (list.size() > 1) {
            Log.warn("shortname \"" + shortname + "\" is ambiguous.");
        }

        var id = template.id();
        var prev = fullnames.put(id, template);
        if (prev != null) {
            Log.error("identifier \"" + id + "\" is ambiguous!");
        }
    }

    @Override
    public <T extends MetaEffectTemplate> void unregister(T template) {
        var prev = fullnames.remove(template.id());
        var list = shortnames.get(template.shortID().toLowerCase());

        // order is very important here.
        if (list == null || !list.removeIf(v -> v.id() == template.id()) || prev == null) {
            Log.warn("attempted to unregister template that is not registered: " + template.id());
        }
    }

    @Override
    public void clear() {
        shortnames.clear();
        fullnames.clear();
    }

    @Override
    public MetaEffectTemplate get(String identifier) {
        var templateList = shortnames.get(identifier.toLowerCase());
        if (templateList != null) {
            var ret = templateList.get(templateList.size() - 1);
            if (templateList.size() != 1) {
                Log.warn("using ambiguous shortname \"" + identifier + "\", returning latest registered: " + ret.id());
            }
            return ret;
        }

        var id = Identifier.tryParse(identifier);
        if (id != null) {
            if (!identifier.contains(":") && id.getNamespace().equalsIgnoreCase("minecraft")) {
                id = new Identifier(Common.namespace, id.getPath());
            }

            var ret = fullnames.get(id);
            if (ret != null) {
                return ret;
            }
        }

        Log.warn("cannot find a template referred to as \"" + identifier + '"');
        return null;
    }
}
