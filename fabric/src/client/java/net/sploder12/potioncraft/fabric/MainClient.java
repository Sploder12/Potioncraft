package net.sploder12.potioncraft.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.sploder12.potioncraft.common.Log;
import net.sploder12.potioncraft.common.PotionCauldronBlockEntity;

@Environment(EnvType.CLIENT)
public class MainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Log.debug("Registering Client...");

        BlockEntityRendererFactories.register(PotionCauldronBlockEntity.POTION_CAULDRON_BLOCK_ENTITY, PotionCauldronRenderer::new);
    }
}
