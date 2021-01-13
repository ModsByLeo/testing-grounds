package adudecalledleo.entityevents.api;

import adudecalledleo.entityevents.impl.EntityTickEventsInternals;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnusedReturnValue")
public interface EntityTickEvents<T extends Entity> {
    static <T extends Entity> @NotNull EntityTickEvents<T> of(EntityType<T> type) {
        return EntityTickEventsInternals.getOrCreate(type);
    }

    // ORDER: ofClass of upper superclass, then ofClass of lower superclasses, then ofClass, then of
    // for example: a zombie would run ofClass(Entity), then ofClass(LivingEntity), then ofClass(HostileEntity),
    // then ofClass(ZombieEntity), then of(EntityType.ZOMBIE)
    static <T extends Entity> @NotNull EntityTickEvents<T> ofClass(Class<T> tClass) {
        return EntityTickEventsInternals.getOrCreateClass(tClass);
    }

    static @NotNull EntityTickEvents<Entity> all() {
        return ofClass(Entity.class);
    }

    static @NotNull EntityTickEvents<LivingEntity> living() {
        return ofClass(LivingEntity.class);
    }

    static @NotNull EntityTickEvents<HostileEntity> hostile() {
        return ofClass(HostileEntity.class);
    }

    static @NotNull EntityTickEvents<AmbientEntity> ambient() {
        return ofClass(AmbientEntity.class);
    }

    static @NotNull EntityTickEvents<PassiveEntity> passive() {
        return ofClass(PassiveEntity.class);
    }

    @NotNull EntityTickEvents<T> registerBefore(Before<T> before);
    @NotNull EntityTickEvents<T> registerAfter(After<T> after);
    @NotNull EntityTickEvents<T> registerCancelled(Cancelled<T> cancelled);

    interface Before<T extends Entity> {
        TriState beforeEntityTick(T entity);
    }

    interface After<T extends Entity> {
        void afterEntityTick(T entity);
    }

    interface Cancelled<T extends Entity> {
        void entityTickCancelled(T entity);
    }
}
