package net.sploder12.potioncraft.meta.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.fluid.Fluids;
import net.sploder12.potioncraft.Main;
import net.sploder12.potioncraft.util.FluidHelper;
import net.sploder12.potioncraft.util.Json;


public interface FluidsParser {
    private static void parseFluids(JsonObject fluids, String file) {
        fluids.asMap().forEach((String fluidId, JsonElement elem) -> {
            if (!elem.isJsonObject()) {
                Main.warn("fluids for " + fluidId + " must be an object " + file);
                return;
            }

            JsonObject fluidEntry = elem.getAsJsonObject();

            Fluid fluid = Json.getRegistryEntry(Identifier.tryParse(fluidId), Registries.FLUID, file);
            if (fluid == null) {
                Main.warn(fluidId + " does not name a fluid " + file);
                return;
            }

            Block defaultBlock = Json.getRegistryEntry(fluidEntry.get("default"), Registries.BLOCK, file);
            if (defaultBlock instanceof AbstractCauldronBlock cauldronBlock) {
                FluidHelper.setDefaultFluidMapping(fluid, cauldronBlock);
            }
            else if (defaultBlock != null) {
                Main.warn(Registries.BLOCK.getId(defaultBlock) + " does not name a cauldron " + file);
            }

            JsonElement cauldronsE = fluidEntry.get("cauldrons");

            if (!cauldronsE.isJsonArray()) {
                Main.warn("cauldrons is not an array! " + file);
                return;
            }

            JsonArray cauldrons = cauldronsE.getAsJsonArray();

            cauldrons.getAsJsonArray().forEach((JsonElement entry) -> {
                if (!entry.isJsonPrimitive()) {
                    Main.warn("fluid entries must be identifiers " + file);
                    return;
                }

                Block block = Json.getRegistryEntry(entry, Registries.BLOCK, file);
                if (block instanceof AbstractCauldronBlock cauldronBlock) {
                    FluidHelper.addFluidMapping(fluid, cauldronBlock);
                }
                else if (block != null) {
                    Main.warn(entry.getAsString() + " does not name a cauldron " + file);
                }
            });
        });
    }


    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            Main.debug("fluids resource no present " + file);
            return;
        }

        if (!elem.isJsonObject()) {
            Main.warn("fluids resource not json object " + file);
            return;
        }

        parseFluids(elem.getAsJsonObject(), file);
    }
}
