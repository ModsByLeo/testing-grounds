package adudecalledleo.entitydamageevents.api;

import adudecalledleo.entitydamageevents.impl.EntityDamageEventsInternals;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedReturnValue")
public interface EntityDamageEvents<T extends Entity> {
    static <T extends Entity> @NotNull EntityDamageEvents<T> of(EntityType<T> type) {
        return EntityDamageEventsInternals.getOrCreate(type);
    }

    // ORDER: ofClass of upper superclass, then ofClass of lower superclasses, then ofClass, then of
    // for example: a zombie would run ofClass(Entity), then ofClass(LivingEntity), then ofClass(HostileEntity),
    // then ofClass(ZombieEntity), then of(EntityType.ZOMBIE)
    static <T extends Entity> @NotNull EntityDamageEvents<T> ofClass(Class<T> tClass) {
        return EntityDamageEventsInternals.getOrCreateClass(tClass);
    }

    static @NotNull EntityDamageEvents<Entity> all() {
        return ofClass(Entity.class);
    }

    static @NotNull EntityDamageEvents<LivingEntity> living() {
        return ofClass(LivingEntity.class);
    }

    static @NotNull EntityDamageEvents<HostileEntity> hostile() {
        return ofClass(HostileEntity.class);
    }

    static @NotNull EntityDamageEvents<AmbientEntity> ambient() {
        return ofClass(AmbientEntity.class);
    }

    static @NotNull EntityDamageEvents<PassiveEntity> passive() {
        return ofClass(PassiveEntity.class);
    }

    @NotNull EntityDamageEvents<T> registerBefore(Before<T> before);
    @NotNull EntityDamageEvents<T> registerAfter(After<T> after);
    @NotNull EntityDamageEvents<T> registerCancelled(Cancelled<T> cancelled);

    interface Before<T extends Entity> {
        TriState beforeEntityDamage(T target, DamageSource source, float amount);
    }

    interface After<T extends Entity> {
        void afterEntityDamage(T target, DamageSource source, float amount);
    }

    interface Cancelled<T extends Entity> {
        void entityDamageCancelled(T target, DamageSource source, float amount);
    }
}
