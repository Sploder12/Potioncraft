package net.sploder12.potioncraft;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;


public class PotionCauldronBlockEntity extends BlockEntity {


    private int level = PotionCauldronBlock.MIN_LEVEL;

    private HashMap<StatusEffect, StatusEffectInstance> effects = new HashMap<StatusEffect, StatusEffectInstance>();

    public static BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(PotionCauldronBlockEntity::new, PotionCauldronBlock.POTION_CAULDRON_BLOCK)
            .build();

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(POTION_CAULDRON_BLOCK_ENTITY, pos, state);
    }

    public static void register() {
        Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                PotionCauldronBlock.POTION_CAULDRON_ID,
                POTION_CAULDRON_BLOCK_ENTITY
        );
    }

    public int getLevel() {
        return level;
    }

    public int getColor() {
        return PotionUtil.getColor(effects.values());
    }

    public ItemStack setEffects(ItemStack target) {
        return PotionUtil.setCustomPotionEffects(target, effects.values().stream().toList());
    }

    public Potion getPotion() {
        return new Potion(effects.values().toArray(new StatusEffectInstance[0]));
    }

    public List<StatusEffectInstance> getEffects() {
        return this.effects.values().stream().toList();
    }

    public void setLevel(int newLevel) {
        if (newLevel != level) {
            level = newLevel;
            markDirty();
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("level", level);

        NbtList nbtList = new NbtList();

        for (StatusEffectInstance effect : effects.values()) {
            if (effect != null) {
                nbtList.add(effect.writeNbt(new NbtCompound()));
            }
        }
        nbt.put("effects", nbtList);

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        effects.clear();
        NbtList nbtList = nbt.getList("effects", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            StatusEffectInstance effect = StatusEffectInstance.fromNbt(nbtCompound);
            if (effect != null) {
                effects.put(effect.getEffectType(), effect);
            }
        }

        level = nbt.getInt("level");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

}
