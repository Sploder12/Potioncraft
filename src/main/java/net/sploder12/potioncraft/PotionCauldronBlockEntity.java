package net.sploder12.potioncraft;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class PotionCauldronBlockEntity extends BlockEntity {


    private int level = PotionCauldronBlock.MIN_LEVEL;

    private HashMap<StatusEffect, PotionEffectInstance> effects = new HashMap<StatusEffect, PotionEffectInstance>();

    public static BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(PotionCauldronBlockEntity::new, PotionCauldronBlock.POTION_CAULDRON_BLOCK)
            .build();

    public static Potion CRAFTED_POTION = Registry.register(Registries.POTION,
            new Identifier("potioncraft", "crafted_potion"),
            new Potion());

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
        return PotionUtil.getColor(getEffects());
    }

    public ItemStack setEffects(ItemStack target) {
        return PotionUtil.setCustomPotionEffects(target, getEffects());
    }

    public Potion getPotion() {
        return new Potion(getEffects().toArray(new StatusEffectInstance[0]));
    }

    public ArrayList<StatusEffectInstance> getEffects() {
        ArrayList<StatusEffectInstance> effects = new ArrayList<>(this.effects.size());

        for (PotionEffectInstance effect : this.effects.values()) {
            effects.add(effect.asStatusEffect());
        }

        return effects;
    }

    public void setLevel(int newLevel) {
        if (newLevel != level) {
            level = newLevel;
            markDirty();
        }
    }

    public boolean addLevel(Collection<StatusEffectInstance> effects) {
        if (level >= PotionCauldronBlock.MAX_LEVEL) return false;

        level += 1;

        float oldDilution = (float)(level - 1) / (float)(level);
        for (PotionEffectInstance effect : this.effects.values()) {
            effect.dilute(oldDilution);
        }

        float newDilution = 1.0f / (float)(level);
        for (StatusEffectInstance effect : effects) {
            PotionEffectInstance peffect = new PotionEffectInstance(effect);
            peffect.dilute(newDilution);

            if (this.effects.containsKey(effect.getEffectType())) {
                PotionEffectInstance cur = this.effects.get(effect.getEffectType());

                cur.duration += peffect.duration;
                cur.amplifier += peffect.amplifier;
                cur.showIcon |= peffect.showIcon;
                cur.showParticles |= peffect.showParticles;
                cur.ambient |= peffect.ambient;
            }
            else {
                this.effects.put(effect.getEffectType(), peffect);
            }
        }

        markDirty();
        return true;
    }

    public ItemStack pickupFluid() {
        if (level <= PotionCauldronBlock.MIN_LEVEL) {
            return null;
        }

        ItemStack potion = new ItemStack(Items.POTION);
        PotionUtil.setPotion(potion, CRAFTED_POTION);
        setEffects(potion);

        level -= 1;

        markDirty();
        return potion;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("level", level);

        NbtList nbtList = new NbtList();

        for (PotionEffectInstance effect : this.effects.values()) {
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
            PotionEffectInstance effect = PotionEffectInstance.fromNbt(nbtCompound);
            if (effect != null) {
                effects.put(effect.type, effect);
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

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isClient()) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (PotionEffectInstance effect : effects.values()) {
            str.append(effect.toString());
        }

        return str.toString();
    }
}
