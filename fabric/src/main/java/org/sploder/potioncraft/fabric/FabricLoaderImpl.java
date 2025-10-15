package org.sploder.potioncraft.fabric;

import java.io.File;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import org.sploder.potioncraft.common.Loader;
import org.sploder.potioncraft.common.PotionCauldronBlock;
import org.sploder.potioncraft.common.PotionCauldronBlockEntity;
import org.sploder.potioncraft.common.config.Config;

public class FabricLoaderImpl implements Loader {

    private final File configFile;
    private final BlockEntityType<PotionCauldronBlockEntity> cauldronEntity;

    FabricLoaderImpl() {
        var path = FabricLoader.getInstance().getConfigDir();
        configFile = path.resolve(Config.filename + ".properties").toFile();

        cauldronEntity = FabricBlockEntityTypeBuilder.create(PotionCauldronBlockEntity::new, PotionCauldronBlock.POTION_CAULDRON_BLOCK)
                .build();
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    @Override
    public BlockEntityType<PotionCauldronBlockEntity> getPotionCauldronBlockEntity() {
        return cauldronEntity;
    }
}

