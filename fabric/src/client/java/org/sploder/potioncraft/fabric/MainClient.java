package org.sploder.potioncraft.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.sploder.potioncraft.common.CommonClient;

@Environment(EnvType.CLIENT)
public class MainClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CommonClient.initialize(new FabricClientLoader());
    }
}
