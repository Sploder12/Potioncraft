package org.sploder.potioncraft.common;

import net.minecraft.block.entity.BlockEntityType;

import java.io.File;

public interface Loader {
    File getConfigFile();

    BlockEntityType<PotionCauldronBlockEntity> getPotionCauldronBlockEntity();
}

