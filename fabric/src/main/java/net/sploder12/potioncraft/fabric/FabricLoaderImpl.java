package net.sploder12.potioncraft.fabric;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.sploder12.potioncraft.common.Loader;
import net.sploder12.potioncraft.common.PotionCauldronBlock;
import net.sploder12.potioncraft.common.PotionCauldronBlockEntity;
import net.sploder12.potioncraft.common.config.Config;
import net.sploder12.potioncraft.fabric.mixin.BehaviorAccessor;

public class FabricLoaderImpl implements Loader {

    private final File configFile;
    private final BlockEntityType<PotionCauldronBlockEntity> cauldronEntity;

    FabricLoaderImpl() {
        Path path = FabricLoader.getInstance().getConfigDir();
        configFile = path.resolve(Config.filename + ".properties").toFile();

        cauldronEntity = FabricBlockEntityTypeBuilder.create(PotionCauldronBlockEntity::new, PotionCauldronBlock.POTION_CAULDRON_BLOCK)
                .build();
    }

    @Override
    public Map<Item, CauldronBehavior> getBehaviorMap(AbstractCauldronBlock id) {
        if (id == null) {
            return null;
        }

        return ((BehaviorAccessor) id).getBehaviorMap();
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

