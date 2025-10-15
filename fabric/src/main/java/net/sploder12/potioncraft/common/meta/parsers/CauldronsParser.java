package net.sploder12.potioncraft.common.meta.parsers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.common.Log;
import net.sploder12.potioncraft.common.util.FluidHelper;
import net.sploder12.potioncraft.common.util.Json;

public interface CauldronsParser {

    private static void parseCauldrons(JsonObject cauldrons, String file) {
        cauldrons.asMap().forEach((String cauldronId, JsonElement elem) -> {
            if (!elem.isJsonPrimitive()) {
                Log.warn("fluid for " + cauldronId + " must be a string. " + file);
                return;
            }

            var block = Registries.BLOCK.get(Identifier.tryParse(cauldronId));
            if (block instanceof AbstractCauldronBlock cauldronBlock) {
                var fluid = Json.getRegistryEntry(elem, Registries.FLUID, file);
                if (fluid == null) {
                    return;
                }

                FluidHelper.setStaticBlockMapping(cauldronBlock, fluid);
            }
            else {
                Log.warn("cauldron identifer " + cauldronId + " does not name a cauldron block " + file);
            }
        });
    }

    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            Log.debug("cauldrons resource not present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Log.warn("cauldrons resource not object " + file);
            return;
        }

        parseCauldrons(elem.getAsJsonObject(), file);
    }
}
