package net.sploder12.potioncraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(EnvType.CLIENT)
public class MainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Main.log("Registering Client...");

        BlockEntityRendererFactories.register(PotionCauldronBlockEntity.POTION_CAULDRON_BLOCK_ENTITY, PotionCauldronRenderer::new);

    }
}
