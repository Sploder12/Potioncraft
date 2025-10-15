package net.sploder12.potioncraft.fabric.meta;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.sploder12.potioncraft.common.Common;
import net.sploder12.potioncraft.common.Log;
import net.sploder12.potioncraft.common.config.Config;
import net.sploder12.potioncraft.common.meta.MetaMixing;
import net.sploder12.potioncraft.common.meta.parsers.*;
import net.sploder12.potioncraft.common.meta.templates.MetaEffectTemplate;
import net.sploder12.potioncraft.common.util.FluidHelper;
import net.sploder12.potioncraft.common.util.HeatHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class FabricMetaMixing {
    public static void register() {
        MetaMixing.register();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("potioncraft", "metamixing");
            }

            @Override
            public void reload(ResourceManager manager) {
                // Clear Caches Here

                FluidHelper.reset();
                HeatHelper.reset();

                CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR.clear();
                CauldronBehavior.registerBehavior();

                MetaMixing.interactions.clear();

                InversionsParser.clear();
                MetaEffectTemplate.register();

                // @TODO clear custom behaviors

                Config.loadConfig(Common.instance.getConfigFile()); // test this

                Map<Identifier, Resource> resources = manager.findResources("metamixing", id -> id.toString().endsWith(".json"));
                resources.forEach((id, resource) -> {
                    try (InputStream stream = resource.getInputStream(); JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
                        // gson kinda blows ngl

                        JsonParser parser = new JsonParser();

                        JsonElement rootE = parser.parse(reader);
                        if (rootE == null || !rootE.isJsonObject()) {
                            Log.warn("Encountered malformed resource " + id);
                            return;
                        }

                        JsonObject root = rootE.getAsJsonObject();
                        String file = id.toString();

                        MetaMixing.parsers.forEach((String elemId, Parser elemParser) ->
                            elemParser.parse(root.get(elemId), file));
                    }
                    catch (Exception e) {
                        Log.error("Error occurred while loading resource " + id + "\n" + e.getMessage());
                    }
                });
            }
        });
    }
}
