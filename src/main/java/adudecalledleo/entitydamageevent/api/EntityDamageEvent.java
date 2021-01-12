package adudecalledleo.entitydamageevent.api;

import adudecalledleo.entitydamageevent.impl.EntityDamageEventInternals;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import org.jetbrains.annotations.NotNull;

public interface EntityDamageEvent<T extends Entity> {
    static <T extends Entity> @NotNull EntityDamageEvent<T> of(EntityType<T> type) {
        return EntityDamageEventInternals.getOrCreate(type);
    }

    // ORDER: ofClass of upper superclass, then ofClass of lower superclasses, then ofClass, then of
    // for example: a zombie would run ofClass(Entity), then ofClass(LivingEntity), then ofClass(HostileEntity),
    // then ofClass(ZombieEntity), then of(EntityType.ZOMBIE)
    static <T extends Entity> @NotNull EntityDamageEvent<T> ofClass(Class<T> tClass) {
        return EntityDamageEventInternals.getOrCreateClass(tClass);
    }

    static @NotNull EntityDamageEvent<Entity> all() {
        return ofClass(Entity.class);
    }

    static @NotNull EntityDamageEvent<LivingEntity> living() {
        return ofClass(LivingEntity.class);
    }

    static @NotNull EntityDamageEvent<HostileEntity> hostile() {
        return ofClass(HostileEntity.class);
    }

    static @NotNull EntityDamageEvent<AmbientEntity> ambient() {
        return ofClass(AmbientEntity.class);
    }

    static @NotNull EntityDamageEvent<PassiveEntity> passive() {
        return ofClass(PassiveEntity.class);
    }

    void register(Callback<T> callback);

    interface Callback<T extends Entity> {
        TriState onEntityDamage(T target, DamageSource source, float amount);
    }
}
