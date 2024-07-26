package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.meta.templates.CustomTemplate;
import net.sploder12.potioncraft.meta.templates.MetaEffectTemplate;

public interface TemplatesParser {
    private static void parseTemplates(JsonObject templates, String file) {
        templates.asMap().forEach((String templateId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Main.warn(templateId + " must be an object " + file);
                return;
            }

            parseTemplate(templateId, elem.getAsJsonObject(), file);
        });
    }

    private static boolean parseTemplate(String name, JsonObject template, String file) {
        CustomTemplate out = CustomTemplate.parse(template, name, file);
        if (out == null) {
            Main.warn("could not parse template " + name + ", is it missing effects? " + file);
            return false;
        }

        MetaEffectTemplate.templates.put("${" + name + "}", out);
        return true;
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null) {
            Main.debug("templates not present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Main.warn("templates is not json object " + file);
            return;
        }

        parseTemplates(elem.getAsJsonObject(), file);
    }
}
