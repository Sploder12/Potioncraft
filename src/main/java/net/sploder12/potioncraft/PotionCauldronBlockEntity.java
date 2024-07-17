package net.sploder12.potioncraft;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LavaCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
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
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.sploder12.potioncraft.meta.MetaMixing;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class PotionCauldronBlockEntity extends BlockEntity {

    private Fluid fluid = Fluids.WATER;
    private int level = PotionCauldronBlock.MIN_LEVEL;
    private int potency = 0;
    private int cachedColor = -1;

    private HashMap<StatusEffect, PotionEffectInstance> effects = new HashMap<>();

    public static BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(PotionCauldronBlockEntity::new, PotionCauldronBlock.POTION_CAULDRON_BLOCK)
            .build();

    public static Potion CRAFTED_POTION = Registry.register(Registries.POTION,
            new Identifier("potioncraft", "crafted_potion"),
            new Potion());

    public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(POTION_CAULDRON_BLOCK_ENTITY, pos, state);
    }

    public static void register() {
        Main.log("Registering Potion Cauldron Block Entity...");

        Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                PotionCauldronBlock.POTION_CAULDRON_ID,
                POTION_CAULDRON_BLOCK_ENTITY
        );
    }

    public Fluid getFluid() { return fluid; }

    // remember to update the luminance after you call this!
    public void setFluid(Fluid fluid) {
        fluid = FluidHelper.getStill(fluid);

        if (this.fluid != fluid) {
            this.fluid = fluid;
            markDirty();
        }
    }

    public int getLevel() {
        return level;
    }

    public int getColor() {
        if (cachedColor == -1) {
            if (!hasEffects() && FluidHelper.getStill(fluid) != Fluids.WATER) {
                cachedColor = 0xffffff;
            }
            else {
                cachedColor = PotionUtil.getColor(getEffects());
            }
        }
        return cachedColor;
    }

    // informs light level to the block and renderer
    public int getLuminance() {
        return FluidHelper.getStill(getFluid()).getDefaultState().getBlockState().getLuminance();
    }

    public int getPotency() {
        return potency;
    }

    static public int getMaxPotency() {
        return Config.getInteger(Config.FieldID.MAX_POTENCY);
    }

    public void setPotency(int amount) {
        potency = amount;
        this.markDirty();
    }

    public ItemStack setEffects(ItemStack target) {
        ArrayList<StatusEffectInstance> effects = getEffects();
        ItemStack effected = PotionUtil.setCustomPotionEffects(target, effects);

        if (!effects.isEmpty()) {
            NbtCompound nbt = effected.getNbt();
            if (nbt == null) nbt = createNbt();
            nbt.putInt("potency", getPotency());
            effected.setNbt(nbt);
        }

        return effected;
    }

    public boolean hasEffects() {
        return !this.effects.isEmpty();
    }

    public ArrayList<StatusEffectInstance> getEffects() {
        ArrayList<StatusEffectInstance> effects = new ArrayList<>(this.effects.size());

        for (PotionEffectInstance effect : this.effects.values()) {
            effects.add(effect.asStatusEffect());
        }

        return effects;
    }

    public void clearEffects() {
        this.effects.clear();
        this.markDirty();
    }

    public PotionEffectInstance getEffect(StatusEffect effect) {
        return effects.get(effect);
    }

    public void setLevel(int newLevel) {
        if (newLevel != level) {
            level = newLevel;
            markDirty();
        }
    }

    public void addEffect(float dilution, PotionEffectInstance effect) {
        effect.dilute(dilution);

        // hacky fix for instant potions
        if (effect.duration < 1.0f) {
            effect.duration = 1.0f;
        }

        if (this.effects.containsKey(effect.type)) {
            this.effects.get(effect.type).combine(effect);
        }
        else {
            this.effects.put(effect.type, effect);
        }

        markDirty();
    }

    public boolean invertEffects() {
        HashMap<StatusEffect, PotionEffectInstance> newEffects = new HashMap<>();

        boolean changed = false;

        for (PotionEffectInstance effect : this.effects.values()) {
            if (MetaMixing.inversions.containsKey(effect.type)) {
                changed = true;
                effect.type = MetaMixing.inversions.get(effect.type);
            }

            if (newEffects.containsKey(effect.type)) {
                newEffects.get(effect.type).combine(effect);
            } else {
                newEffects.put(effect.type, effect);
            }
        }

        if (changed) {
            effects = newEffects;
            markDirty();
        }

        return changed;
    }

    public void extendDuration(float dur) {
        if (this.effects.isEmpty()) return;

        float portion = (dur / this.effects.size()) / this.getLevel();

        for (PotionEffectInstance effect : this.effects.values()) {
            // there should be some balance but that's for future me to do
            effect.duration += portion;
        }

        markDirty();
    }

    public void amplify(float amp) {
        if (this.effects.isEmpty()) return;

        float portion = (amp / this.effects.size()) / this.getLevel();

        for (PotionEffectInstance effect : this.effects.values()) {
            // there should be some balance but that's for future me to do
            effect.amplifier += portion;
        }

        markDirty();
    }

    public boolean addEffects(Collection<StatusEffectInstance> effects) {

        float newDilution = 1.0f / (float)(level);
        for (StatusEffectInstance effect : effects) {
            addEffect(newDilution, new PotionEffectInstance(effect));
        }

        // prune dead effects
        for (PotionEffectInstance effect : this.effects.values()) {
            if (effect.amplifier <= PotionEffectInstance.epsilon || effect.duration < PotionEffectInstance.epsilon) {
                this.effects.remove(effect.type);
            }
        }

        markDirty();
        return true;
    }

    public boolean addLevel(boolean dilute) {
        if (level >= PotionCauldronBlock.MAX_LEVEL) return false;

        level += 1;

        if (dilute) {
            float oldDilution = (float) (level - 1) / (float) (level);
            for (PotionEffectInstance effect : this.effects.values()) {
                effect.dilute(oldDilution);
            }

            // prune dead effects
            for (PotionEffectInstance effect : this.effects.values()) {
                if (effect.amplifier <= PotionEffectInstance.epsilon || effect.duration < PotionEffectInstance.epsilon) {
                    this.effects.remove(effect.type);
                }
            }
        }

        markDirty();
        return true;
    }

    public boolean removeLevel() {
        if (level < PotionCauldronBlock.MIN_LEVEL) {
            return false;
        }

        level -= 1;

        markDirty();
        return true;
    }

    @Deprecated
    public ItemStack pickupFluid() {

        ItemStack potion = new ItemStack(Items.POTION);

        if (effects.isEmpty()) {
            PotionUtil.setPotion(potion, Potions.WATER);
        }
        else {
            PotionUtil.setPotion(potion, CRAFTED_POTION);
            setEffects(potion);
        }

        level -= 1;

        markDirty();
        return potion;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("level", level);
        nbt.putInt("potency", potency);

        NbtList nbtList = new NbtList();

        for (PotionEffectInstance effect : this.effects.values()) {
            if (effect != null) {
                nbtList.add(effect.writeNbt(new NbtCompound()));
            }
        }
        nbt.put("effects", nbtList);

        nbt.putString("fluid", Registries.FLUID.getId(fluid).toString());

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        Identifier fluidId = Identifier.tryParse(nbt.getString("fluid"));

        if (fluidId != null) {
            fluid = Registries.FLUID.get(fluidId);
        }
        else {
            fluid = Fluids.EMPTY;
        }

        effects.clear();
        NbtList nbtList = nbt.getList("effects", NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            PotionEffectInstance effect = PotionEffectInstance.fromNbt(nbtCompound);
            if (effect != null) {
                effects.put(effect.type, effect);
            }
        }

        potency = nbt.getInt("potency");
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
        cachedColor = -1;

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
