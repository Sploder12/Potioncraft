package net.sploder12.potioncraft.meta.parsers;

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
            if (!elem.isJsonArray()) {
                Main.log("WARNING: fluids for " + fluidId + " must be an array " + file);
                return;
            }

            Fluid fluid = Registries.FLUID.get(Identifier.tryParse(fluidId));
            if (fluid == Fluids.EMPTY) {
                Main.log("WARNING: " + fluidId + " does not name a fluid " + file);
                return;
            }

            elem.getAsJsonArray().forEach((JsonElement entry) -> {
                if (!entry.isJsonPrimitive()) {
                    Main.log("WARNING: fluid entries must be identifiers " + file);
                    return;
                }

                Block block = Json.getRegistryEntry(entry, Registries.BLOCK);
                if (block instanceof AbstractCauldronBlock cauldronBlock) {
                    FluidHelper.addFluidMapping(fluid, cauldronBlock);
                }
                else {
                    Main.log("WARNING: fluid entries must identify cauldron blocks " + file);
                }
            });
        });
    }


    static void parse(JsonElement elem, String file) {
        if (elem == null || !elem.isJsonObject()) {
            return;
        }

        if (!elem.isJsonObject()) {
            Main.log("WARNING: fluids resource not object" + file);
            return;
        }

        parseFluids(elem.getAsJsonObject(), file);
    }
}
