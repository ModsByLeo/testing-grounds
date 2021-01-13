package adudecalledleo.entityevents.impl;

import adudecalledleo.entityevents.api.EntityDamageEvents;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@ApiStatus.Internal
public final class EntityDamageEventsInternals {
    public static class Impl<T extends Entity> implements EntityDamageEvents<T> {
        private final Event<Before<T>> beforeEvent;
        private final Event<After<T>> afterEvent;
        private final Event<Cancelled<T>> cancelledEvent;

        public Impl() {
            beforeEvent = EventFactory.createArrayBacked(Before.class, befores -> (target, source, amount) -> {
                TriState ret = TriState.DEFAULT;
                for (Before<T> before : befores) {
                    ret = before.beforeEntityDamage(target, source, amount);
                    if (ret == TriState.DEFAULT)
                        continue;
                    return ret;
                }
                return ret;
            });
            afterEvent = EventFactory.createArrayBacked(After.class, afters -> (target, source, amount) -> {
                for (After<T> after : afters)
                    after.afterEntityDamage(target, source, amount);
            });
            cancelledEvent = EventFactory.createArrayBacked(Cancelled.class, cancelleds -> (target, source, amount) -> {
                for (Cancelled<T> cancelled : cancelleds)
                    cancelled.entityDamageCancelled(target, source, amount);
            });
        }

        @Override
        public @NotNull EntityDamageEvents<T> registerBefore(Before<T> before) {
            beforeEvent.register(before);
            return this;
        }

        @Override
        public @NotNull EntityDamageEvents<T> registerAfter(After<T> after) {
            afterEvent.register(after);
            return this;
        }

        @Override
        public @NotNull EntityDamageEvents<T> registerCancelled(Cancelled<T> cancelled) {
            cancelledEvent.register(cancelled);
            return this;
        }
    }

    private static final ReferenceOpenHashSet<Entity> ENTITIES_HURT_THIS_TICK =
            new ReferenceOpenHashSet<>();
    private static final Reference2ReferenceOpenHashMap<EntityType<? extends Entity>, Impl<? extends Entity>> DAMAGE_EVENTS =
            new Reference2ReferenceOpenHashMap<>();
    private static final Reference2ReferenceOpenHashMap<Class<? extends Entity>, Impl<? extends Entity>> CLASS_DAMAGE_EVENTS =
            new Reference2ReferenceOpenHashMap<>();

    static void initialize() {
        ServerTickEvents.START_WORLD_TICK.register(world -> ENTITIES_HURT_THIS_TICK.clear());
    }

    public static <T extends Entity> @NotNull EntityDamageEvents<T> getOrCreate(EntityType<T> type) {
        //noinspection unchecked
        return (EntityDamageEvents<T>) DAMAGE_EVENTS.computeIfAbsent(type, type1 -> new Impl<>());
    }

    public static <T extends Entity> @NotNull EntityDamageEvents<T> getOrCreateClass(Class<T> clazz) {
        //noinspection unchecked
        return (EntityDamageEvents<T>) CLASS_DAMAGE_EVENTS.computeIfAbsent(clazz, clazz1 -> new Impl<>());
    }

    @SuppressWarnings("RedundantSuppression")
    private static <T extends Entity> @NotNull TriState invokeSuperBefore(T target, DamageSource source, float amount,
            Class<? extends Entity> clazz) {
        TriState state = TriState.DEFAULT;
        if (Entity.class.isAssignableFrom(clazz.getSuperclass()))
            //noinspection unchecked
            state = invokeSuperBefore(target, source, amount, (Class<? extends Entity>) clazz.getSuperclass());
        if (state != TriState.DEFAULT)
            return state;
        if (!CLASS_DAMAGE_EVENTS.containsKey(clazz))
            return TriState.DEFAULT;
        //noinspection RedundantCast,unchecked
        return ((Impl<T>) CLASS_DAMAGE_EVENTS.get(clazz)).beforeEvent.invoker().beforeEntityDamage(target, source, amount);
    }

    public static <T extends Entity> boolean invoke(T target, DamageSource source, float amount) {

        if (!ENTITIES_HURT_THIS_TICK.add(target))
            return false;
        TriState state = invokeSuperBefore(target, source, amount, target.getClass());
        boolean ret = false;
        if (state != TriState.DEFAULT)
            ret = state.get();
        else {
            //noinspection unchecked
            Impl<T> impl = (Impl<T>) DAMAGE_EVENTS.get(target.getType());
            if (impl != null)
                ret = impl.beforeEvent.invoker().beforeEntityDamage(target, source, amount).orElse(false);
        }
        if (ret)
            invokeCancelled(target, source, amount);
        else
            invokeAfter(target, source, amount);
        return ret;
    }

    private static <T extends Entity> void invokeSuper(Class<? extends Entity> clazz, Consumer<Impl<T>> consumer) {
        if (Entity.class.isAssignableFrom(clazz.getSuperclass()))
            //noinspection unchecked
            invokeSuper((Class<? extends Entity>) clazz.getSuperclass(), consumer);
        if (!CLASS_DAMAGE_EVENTS.containsKey(clazz))
            return;
        //noinspection unchecked
        consumer.accept((Impl<T>) CLASS_DAMAGE_EVENTS.get(clazz));
    }

    private static <T extends Entity> void invokeCancelled(T target, DamageSource source, float amount) {
        invokeSuper(target.getClass(), impl -> impl.cancelledEvent.invoker().entityDamageCancelled(target, source, amount));
        //noinspection unchecked
        Impl<T> impl = (Impl<T>) DAMAGE_EVENTS.get(target.getType());
        if (impl == null)
            return;
        impl.cancelledEvent.invoker().entityDamageCancelled(target, source, amount);
    }

    // TODO this isn't actually called after damage gets applied but I don't care enough to implement that
    private static <T extends Entity> void invokeAfter(T target, DamageSource source, float amount) {
        invokeSuper(target.getClass(), impl -> impl.afterEvent.invoker().afterEntityDamage(target, source, amount));
        //noinspection unchecked
        Impl<T> impl = (Impl<T>) DAMAGE_EVENTS.get(target.getType());
        if (impl == null)
            return;
        impl.afterEvent.invoker().afterEntityDamage(target, source, amount);
    }
}
