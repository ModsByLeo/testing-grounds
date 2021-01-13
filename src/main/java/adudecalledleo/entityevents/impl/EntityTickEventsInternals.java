package adudecalledleo.entityevents.impl;

import adudecalledleo.entityevents.api.EntityTickEvents;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@ApiStatus.Internal
public final class EntityTickEventsInternals {
    public static class Impl<T extends Entity> implements EntityTickEvents<T> {
        private final Event<Before<T>> beforeEvent;
        private final Event<After<T>> afterEvent;
        private final Event<Cancelled<T>> cancelledEvent;

        public Impl() {
            beforeEvent = EventFactory.createArrayBacked(Before.class, befores -> target -> {
                TriState ret = TriState.DEFAULT;
                for (Before<T> before : befores) {
                    ret = before.beforeEntityTick(target);
                    if (ret == TriState.DEFAULT)
                        continue;
                    return ret;
                }
                return ret;
            });
            afterEvent = EventFactory.createArrayBacked(After.class, afters -> target -> {
                for (After<T> after : afters)
                    after.afterEntityTick(target);
            });
            cancelledEvent = EventFactory.createArrayBacked(Cancelled.class, cancelleds -> target -> {
                for (Cancelled<T> cancelled : cancelleds)
                    cancelled.entityTickCancelled(target);
            });
        }

        @Override
        public @NotNull EntityTickEvents<T> registerBefore(Before<T> before) {
            beforeEvent.register(before);
            return this;
        }

        @Override
        public @NotNull EntityTickEvents<T> registerAfter(After<T> after) {
            afterEvent.register(after);
            return this;
        }

        @Override
        public @NotNull EntityTickEvents<T> registerCancelled(Cancelled<T> cancelled) {
            cancelledEvent.register(cancelled);
            return this;
        }
    }

    private static final ReferenceOpenHashSet<Entity> ENTITIES_INVOKED_THIS_TICK =
            new ReferenceOpenHashSet<>();
    private static final Reference2ReferenceOpenHashMap<EntityType<? extends Entity>, Impl<? extends Entity>> TICK_EVENTS =
            new Reference2ReferenceOpenHashMap<>();
    private static final Reference2ReferenceOpenHashMap<Class<? extends Entity>, Impl<? extends Entity>> CLASS_TICK_EVENTS =
            new Reference2ReferenceOpenHashMap<>();

    static void initialize() {
        ServerTickEvents.START_WORLD_TICK.register(world -> ENTITIES_INVOKED_THIS_TICK.clear());
    }

    public static <T extends Entity> @NotNull EntityTickEvents<T> getOrCreate(EntityType<T> type) {
        //noinspection unchecked
        return (EntityTickEvents<T>) TICK_EVENTS.computeIfAbsent(type, type1 -> new Impl<>());
    }

    public static <T extends Entity> @NotNull EntityTickEvents<T> getOrCreateClass(Class<T> clazz) {
        //noinspection unchecked
        return (EntityTickEvents<T>) CLASS_TICK_EVENTS.computeIfAbsent(clazz, clazz1 -> new Impl<>());
    }

    @SuppressWarnings("RedundantSuppression")
    private static <T extends Entity> @NotNull TriState invokeSuperBefore(T target,
            Class<? extends Entity> clazz) {
        TriState state = TriState.DEFAULT;
        if (Entity.class.isAssignableFrom(clazz.getSuperclass()))
            //noinspection unchecked
            state = invokeSuperBefore(target, (Class<? extends Entity>) clazz.getSuperclass());
        if (state != TriState.DEFAULT)
            return state;
        if (!CLASS_TICK_EVENTS.containsKey(clazz))
            return TriState.DEFAULT;
        //noinspection RedundantCast,unchecked
        return ((Impl<T>) CLASS_TICK_EVENTS.get(clazz)).beforeEvent.invoker().beforeEntityTick(target);
    }

    public static <T extends Entity> boolean invoke(T entity) {
        if (!ENTITIES_INVOKED_THIS_TICK.add(entity))
            return false;
        TriState state = invokeSuperBefore(entity, entity.getClass());
        boolean ret = false;
        if (state != TriState.DEFAULT)
            ret = state.get();
        else {
            //noinspection unchecked
            Impl<T> impl = (Impl<T>) TICK_EVENTS.get(entity.getType());
            if (impl != null)
                ret = impl.beforeEvent.invoker().beforeEntityTick(entity).orElse(false);
        }
        if (ret)
            invokeCancelled(entity);
        else
            invokeAfter(entity);
        return ret;
    }

    private static <T extends Entity> void invokeSuper(Class<? extends Entity> clazz, Consumer<Impl<T>> consumer) {
        if (Entity.class.isAssignableFrom(clazz.getSuperclass()))
            //noinspection unchecked
            invokeSuper((Class<? extends Entity>) clazz.getSuperclass(), consumer);
        if (!CLASS_TICK_EVENTS.containsKey(clazz))
            return;
        //noinspection unchecked
        consumer.accept((Impl<T>) CLASS_TICK_EVENTS.get(clazz));
    }

    private static <T extends Entity> void invokeCancelled(T entity) {
        invokeSuper(entity.getClass(), impl -> impl.cancelledEvent.invoker().entityTickCancelled(entity));
        //noinspection unchecked
        Impl<T> impl = (Impl<T>) TICK_EVENTS.get(entity.getType());
        if (impl == null)
            return;
        impl.cancelledEvent.invoker().entityTickCancelled(entity);
    }

    // TODO this isn't actually called after the tick but I don't care enough to implement that
    private static <T extends Entity> void invokeAfter(T entity) {
        invokeSuper(entity.getClass(), impl -> impl.afterEvent.invoker().afterEntityTick(entity));
        //noinspection unchecked
        Impl<T> impl = (Impl<T>) TICK_EVENTS.get(entity.getType());
        if (impl == null)
            return;
        impl.afterEvent.invoker().afterEntityTick(entity);
    }
}
