package adudecalledleo.entityevents.api;

import adudecalledleo.entityevents.impl.EntityDamageEventsInternals;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedReturnValue")
public interface EntityDamageEvents<T extends Entity> {
    static <T extends Entity> @NotNull EntityDamageEvents<T> of(@NotNull EntityType<T> type) {
        return EntityDamageEventsInternals.getOrCreate(type);
    }

    static @NotNull EntityDamageEvents<Entity> inTag(@NotNull Tag<EntityType<?>> tag) {
        return EntityDamageEventsInternals.getOrCreateTag(tag);
    }

    static <T extends Entity> @NotNull EntityDamageEvents<T> ofClass(@NotNull Class<T> tClass) {
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