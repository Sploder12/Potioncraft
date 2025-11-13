package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.sploder.potioncraft.common.Common;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.meta.templates.CustomTemplate;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;
import org.sploder.potioncraft.common.meta.templates.TemplateResolver;

public interface TemplatesParser {
    private static void parseTemplates(TemplateResolver resolver, JsonObject templates, String file) {
        templates.asMap().forEach((String templateId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Log.warn(templateId + " must be an object " + file);
                return;
            }

            parseTemplate(resolver, templateId, elem.getAsJsonObject(), file);
        });
    }

    private static boolean parseTemplate(TemplateResolver resolver, String name, JsonObject template, String file) {
        CustomTemplate out = CustomTemplate.parse(resolver, template, name, file);
        if (out == null) {
            Log.warn("could not parse template " + name + ", is it missing effects? " + file);
            return false;
        }

        resolver.register(out);
        return true;
    }

    static void parse(TemplateResolver resolver, JsonElement elem, String file) {
        if (elem == null) {
            Log.debug("templates not present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Log.warn("templates is not json object " + file);
            return;
        }

        parseTemplates(resolver, elem.getAsJsonObject(), file);
    }
}
