package adudecalledleo.serversiding.util;

import net.minecraft.enchantment.Enchantment;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EnchantMapBuilder {
    private final LinkedHashMap<Enchantment, Integer> map;

    private EnchantMapBuilder() {
        map = new LinkedHashMap<>();
    }

    /**
     * Creates a new {@code EnchantMapBuilder}.
     *
     * @return a new builder instance
     */
    public static EnchantMapBuilder create() {
        return new EnchantMapBuilder();
    }

    /**
     * Adds an enchantment-level pair entry of the specified enchantment and level to the builder.
     *
     * @param enchantment
     *         enchantment
     * @param level
     *         level of enchantment
     * @return this builder
     */
    public EnchantMapBuilder add(Enchantment enchantment, int level) {
        map.remove(enchantment);
        map.put(enchantment, level);
        return this;
    }

    /**
     * Adds an enchantment-level pair entry of the specified enchantment and its minimum level to the builder.
     *
     * @param enchantment
     *         enchantment
     * @return this builder
     */
    public EnchantMapBuilder addMin(Enchantment enchantment) {
        return add(enchantment, enchantment.getMinLevel());
    }

    /**
     * Adds an enchantment-level pair entry of the specified enchantment and its maximum level to the builder.
     *
     * @param enchantment
     *         enchantment
     * @return this builder
     */
    public EnchantMapBuilder addMax(Enchantment enchantment) {
        return add(enchantment, enchantment.getMaxLevel());
    }

    /**
     * Builds the final {@code Enchantment}s to levels map.
     *
     * @return the {@code Enchantment}s to levels map
     */
    public Map<Enchantment, Integer> build() {
        return new LinkedHashMap<>(map);
    }

    /**
     * Creates a {@linkplain Collections#singletonMap(Object, Object) singleton map} of
     * {@code Enchantment}s to levels containing the specified enchantment and level.
     *
     * @param enchantment
     *         enchantment
     * @param level
     *         level of enchantment
     * @return the {@code Enchantment}s to levels map
     */
    public static Map<Enchantment, Integer> of(Enchantment enchantment, int level) {
        return Collections.singletonMap(enchantment, level);
    }

    /**
     * Creates a {@linkplain Collections#singletonMap(Object, Object) singleton map} of
     * {@code Enchantment}s to levels containing the specified enchantment and its minimum level.
     *
     * @param enchantment
     *         enchantment
     * @return the {@code Enchantment}s to levels map
     */
    public static Map<Enchantment, Integer> ofMin(Enchantment enchantment) {
        return of(enchantment, enchantment.getMinLevel());
    }

    /**
     * Creates a {@linkplain Collections#singletonMap(Object, Object) singleton map} of
     * {@code Enchantment}s to levels containing the specified enchantment and its maximum level.
     *
     * @param enchantment
     *         enchantment
     * @return the {@code Enchantment}s to levels map
     */
    public static Map<Enchantment, Integer> ofMax(Enchantment enchantment) {
        return of(enchantment, enchantment.getMaxLevel());
    }
}
