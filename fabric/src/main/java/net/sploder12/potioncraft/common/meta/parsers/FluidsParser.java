package net.sploder12.potioncraft.common.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.common.Log;
import net.sploder12.potioncraft.common.util.FluidHelper;
import net.sploder12.potioncraft.common.util.Json;


public interface FluidsParser {
    private static void parseFluids(JsonObject fluids, String file) {
        fluids.asMap().forEach((String fluidId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Log.warn("fluids for " + fluidId + " must be an object " + file);
                return;
            }

            JsonObject fluidEntry = elem.getAsJsonObject();

            var fluid = Json.getRegistryEntry(Identifier.tryParse(fluidId), Registries.FLUID, file);
            if (fluid == null) {
                Log.warn(fluidId + " does not name a fluid " + file);
                return;
            }

            var defaultBlock = Json.getRegistryEntry(fluidEntry.get("default"), Registries.BLOCK, file);
            if (defaultBlock instanceof AbstractCauldronBlock cauldronBlock) {
                FluidHelper.setDefaultFluidMapping(fluid, cauldronBlock);
            }
            else if (defaultBlock != null) {
                Log.warn(Registries.BLOCK.getId(defaultBlock) + " does not name a cauldron " + file);
            }

            JsonElement cauldronsE = fluidEntry.get("cauldrons");

            if (!cauldronsE.isJsonArray()) {
                Log.warn("cauldrons is not an array! " + file);
                return;
            }

            JsonArray cauldrons = cauldronsE.getAsJsonArray();

            cauldrons.getAsJsonArray().forEach((JsonElement entry) -> {
                if (!entry.isJsonPrimitive()) {
                    Log.warn("fluid entries must be identifiers " + file);
                    return;
                }

                var block = Json.getRegistryEntry(entry, Registries.BLOCK, file);
                if (block instanceof AbstractCauldronBlock cauldronBlock) {
                    FluidHelper.addFluidMapping(fluid, cauldronBlock);
                }
                else if (block != null) {
                    Log.warn(entry.getAsString() + " does not name a cauldron " + file);
                }
            });
        });
    }


    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            Log.debug("fluids resource no present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Log.warn("fluids resource not json object " + file);
            return;
        }

        parseFluids(elem.getAsJsonObject(), file);
    }
}
