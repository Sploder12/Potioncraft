package net.sploder12.potioncraft;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class PotionEffectInstance {
    public StatusEffect type;
    public float duration;
    public float amplifier;
    public boolean ambient;
    public boolean showParticles;
    public boolean showIcon;

    public static final float epsilon = 0.001f;

    PotionEffectInstance(StatusEffect type) {
        this(type, 0.0f, 1.0f);
    }

    PotionEffectInstance(StatusEffect type, float duration, float amplifier) {
        this(type, duration, amplifier, false, true);
    }

    PotionEffectInstance(StatusEffect type, float duration, float amplifier, boolean ambient, boolean visible) {
        this(type, duration, amplifier, ambient, visible, visible);
    }

    PotionEffectInstance(@NotNull StatusEffectInstance effect) {
        this(effect.getEffectType(), effect.getDuration(), effect.getAmplifier() + 1, effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon());
    }

    PotionEffectInstance(PotionEffectInstance other) {
        this(other.type, other.duration, other.amplifier, other.ambient, other.showParticles, other.showIcon);
    }

    public PotionEffectInstance(StatusEffect type, float duration, float amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.showParticles = showParticles;
        this.showIcon = showIcon;
    }

    // WARNING: ONLY WORKS WITH POTIONS WITH ONLY 1 EFFECT
    public PotionEffectInstance(Potion potion) {
        this(potion.getEffects().get(0));

        assert potion.getEffects().size() == 1;
    }

    public PotionEffectInstance combine(PotionEffectInstance other) {
        this.duration += other.duration;
        this.amplifier += other.amplifier;
        this.showIcon |= other.showIcon;
        this.showParticles |= other.showParticles;
        this.ambient |= other.ambient;

        return this;
    }


    public StatusEffectInstance asStatusEffect() {
        int effectiveDuration;
        int effectiveAmplifier = (int)(amplifier);

        if (amplifier < 1.0f) {
            effectiveDuration = (int)(duration * amplifier);
            effectiveAmplifier = 1;
        }
        else {
            float fract = amplifier - effectiveAmplifier;
            effectiveDuration = (int)(duration * (1.0f + fract)); // adjust duration based of fraction of amplifier
        }

        // there is an implicit +1 to amplifiers (to make fractions work better)

        return new StatusEffectInstance(type, effectiveDuration, effectiveAmplifier - 1, ambient, showParticles, showIcon);
    }

    public void dilute(float ratio) {
        duration *= ratio;
        amplifier *= ratio;

        if (duration - epsilon <= 0.0f) {
            duration = 0.0f;
        }

        if (amplifier - epsilon <= 0.0f) {
            amplifier = 0.0f;
        }
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return asStatusEffect().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof PotionEffectInstance other) {
            return
                    Math.abs(this.duration - other.duration) <= epsilon &&
                    Math.abs(this.amplifier - other.amplifier) <= epsilon &&
                    this.ambient == other.ambient &&
                    this.showParticles == other.showParticles &&
                    this.showIcon == other.showIcon;
        }

        return false;
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("Id", StatusEffect.getRawId(this.type));
        this.writeTypelessNbt(nbt);
        return nbt;
    }

    private void writeTypelessNbt(NbtCompound nbt) {
        nbt.putFloat("Amplifier", this.amplifier);
        nbt.putFloat("Duration", this.duration);
        nbt.putBoolean("Ambient", this.ambient);
        nbt.putBoolean("ShowParticles", this.showParticles);
        nbt.putBoolean("ShowIcon", this.showIcon);
    }

    @Nullable
    public static PotionEffectInstance fromNbt(NbtCompound nbt) {
        int id = nbt.getInt("Id");
        StatusEffect type = StatusEffect.byRawId(id);
        return type == null ? null : fromNbt(type, nbt);
    }

    private static PotionEffectInstance fromNbt(StatusEffect type, NbtCompound nbt) {
        float amp = nbt.getFloat("Amplifier");
        float dur = nbt.getFloat("Duration");

        boolean ambient = nbt.getBoolean("Ambient");
        boolean particles = true;
        if (nbt.contains("ShowParticles", 1)) {
            particles = nbt.getBoolean("ShowParticles");
        }

        boolean icon = particles;
        if (nbt.contains("ShowIcon", 1)) {
            icon = nbt.getBoolean("ShowIcon");
        }

        return new PotionEffectInstance(type, dur, amp, ambient, particles, icon);
    }
}
