package net.sploder12.potioncraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class MainClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("PotionCraft");


    @Override
    public void onInitializeClient() {
        LOGGER.info("Registering Client...");

        BlockEntityRendererFactories.register(PotionCauldronBlockEntity.POTION_CAULDRON_BLOCK_ENTITY, PotionCauldronRenderer::new);

    }
}
