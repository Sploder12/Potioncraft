package org.sploder.potioncraft.common.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.sploder.potioncraft.common.Log;
import org.sploder.potioncraft.common.meta.templates.CustomTemplate;
import org.sploder.potioncraft.common.meta.templates.MetaEffectTemplate;

public interface TemplatesParser {
    private static void parseTemplates(JsonObject templates, String file) {
        templates.asMap().forEach((String templateId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Log.warn(templateId + " must be an object " + file);
                return;
            }

            parseTemplate(templateId, elem.getAsJsonObject(), file);
        });
    }

    private static boolean parseTemplate(String name, JsonObject template, String file) {
        CustomTemplate out = CustomTemplate.parse(template, name, file);
        if (out == null) {
            Log.warn("could not parse template " + name + ", is it missing effects? " + file);
            return false;
        }

        MetaEffectTemplate.templates.put("${" + name + "}", out);
        return true;
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null) {
            Log.debug("templates not present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Log.warn("templates is not json object " + file);
            return;
        }

        parseTemplates(elem.getAsJsonObject(), file);
    }
}
