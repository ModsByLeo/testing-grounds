package adudecalledleo.entitydamageevent.impl;

import adudecalledleo.entitydamageevent.api.EntityDamageEvent;
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

@ApiStatus.Internal
public final class EntityDamageEventInternals {
    public static class Impl<T extends Entity> implements EntityDamageEvent<T> {
        private final Event<Callback<T>> event;

        public Impl() {
            event = EventFactory.createArrayBacked(Callback.class, callbacks -> (target, source, amount) -> {
                TriState ret = TriState.DEFAULT;
                for (Callback<T> callback : callbacks) {
                    ret = callback.onEntityDamage(target, source, amount);
                    if (ret == TriState.DEFAULT)
                        continue;
                    return ret;
                }
                return ret;
            });
        }

        @Override
        public void register(Callback<T> callback) {
            event.register(callback);
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

    public static <T extends Entity> @NotNull EntityDamageEvent<T> getOrCreate(EntityType<T> type) {
        //noinspection unchecked
        return (EntityDamageEvent<T>) DAMAGE_EVENTS.computeIfAbsent(type, type1 -> new Impl<>());
    }

    public static <T extends Entity> @NotNull EntityDamageEvent<T> getOrCreateClass(Class<T> clazz) {
        //noinspection unchecked
        return (EntityDamageEvent<T>) CLASS_DAMAGE_EVENTS.computeIfAbsent(clazz, clazz1 -> new Impl<>());
    }

    @SuppressWarnings("RedundantSuppression")
    private static <T extends Entity> @NotNull TriState invokeSuper(T target, DamageSource source, float amount, Class<? extends Entity> clazz) {
        TriState state = TriState.DEFAULT;
        if (Entity.class.isAssignableFrom(clazz.getSuperclass()))
            //noinspection unchecked
            state = invokeSuper(target, source, amount, (Class<? extends Entity>) clazz.getSuperclass());
        if (!CLASS_DAMAGE_EVENTS.containsKey(clazz))
            return TriState.DEFAULT;
        //noinspection RedundantCast,unchecked
        return ((Impl<T>) CLASS_DAMAGE_EVENTS.get(clazz)).event.invoker().onEntityDamage(target, source, amount);
    }

    public static <T extends Entity> boolean invoke(T target, DamageSource source, float amount) {
        if (!ENTITIES_HURT_THIS_TICK.add(target))
            return false;
        TriState state = invokeSuper(target, source, amount, target.getClass());
        if (state != TriState.DEFAULT)
            return !state.get();
        //noinspection unchecked
        Impl<T> impl = (Impl<T>) DAMAGE_EVENTS.get(target.getType());
        if (impl == null)
            return false;
        return !impl.event.invoker().onEntityDamage(target, source, amount).orElse(false);
    }
}
