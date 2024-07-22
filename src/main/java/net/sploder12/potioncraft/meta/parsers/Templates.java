package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.meta.templates.CustomTemplate;
import net.sploder12.potioncraft.meta.templates.MetaEffectTemplate;

public interface Templates {
    private static void parseTemplates(JsonObject templates, String file) {
        templates.asMap().forEach((String templateId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Main.log("WARNING: " + templateId + " must be an object " + file);
                return;
            }

            parseTemplate(templateId, elem.getAsJsonObject(), file);
        });
    }

    private static boolean parseTemplate(String name, JsonObject template, String file) {
        CustomTemplate out = CustomTemplate.parse(template, name, file);
        if (out == null) {
            Main.log("WARNING: could not parse template " + name + ", is it missing effects? " + file);
            return false;
        }

        MetaEffectTemplate.templates.put("${" + name + "}", out);
        return true;
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null) {
            return;
        }

        if (!elem.isJsonObject()) {
            Main.log("WARNING: templates is not json object " + file);
            return;
        }

        parseTemplates(elem.getAsJsonObject(), file);
    }
}
