package adudecalledleo.mcmail.api.meta;

import org.jetbrains.annotations.NotNull;

public final class MetadataKey<V> {
    public static <V> @NotNull MetadataKey<V> of(@NotNull String id, @NotNull MetadataValueType<V> valueType) {
        return new MetadataKey<>(id, valueType);
    }

    private final String id;
    private final MetadataValueType<V> valueType;

    private MetadataKey(String id, MetadataValueType<V> valueType) {
        this.id = id;
        this.valueType = valueType;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull MetadataValueType<V> getValueType() {
        return valueType;
    }

    public @NotNull Class<V> getValueClass() {
        return valueType.getValueClass();
    }
}
