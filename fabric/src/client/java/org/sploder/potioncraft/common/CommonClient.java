package org.sploder.potioncraft.common;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public abstract class CommonClient {

    public static ClientLoader instance = null;

    public static void initialize(ClientLoader loader) {
        instance = loader;

        Log.debug("Registering Client...");

        BlockEntityRendererFactories.register(PotionCauldronBlockEntity.POTION_CAULDRON_BLOCK_ENTITY, PotionCauldronRenderer::new);
    }
}
