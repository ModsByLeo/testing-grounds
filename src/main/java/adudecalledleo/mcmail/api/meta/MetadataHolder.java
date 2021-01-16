package adudecalledleo.mcmail.api.meta;

import adudecalledleo.mcmail.api.Registries;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public final class MetadataHolder {
    private final Object2ReferenceOpenHashMap<MetadataKey<Object>, Object> map;
    private final Set<MetadataKey<?>> keySetView;

    public MetadataHolder() {
        map = new Object2ReferenceOpenHashMap<>();
        keySetView = Collections.unmodifiableSet(map.keySet());
    }

    public <V> boolean containsMetadata(@NotNull MetadataKey<V> key) {
        return map.containsKey(key);
    }

    public <V> @Nullable V getMetadata(@NotNull MetadataKey<V> key) {
        if (!map.containsKey(key))
            return null;
        Object rawValue = map.get(key);
        return key.getValueClass().cast(rawValue);
    }

    public <V> void setMetadata(@NotNull MetadataKey<V> key, @Nullable V value) {
        if (value == null)
            map.remove(key);
        else
            //noinspection unchecked
            map.put((MetadataKey<Object>) key, value);
    }

    public void clearMetadata() {
        map.clear();
    }

    public @NotNull Set<MetadataKey<?>> getMetadataKeys() {
        return keySetView;
    }

    public void fromTag(@NotNull CompoundTag tag) throws InvalidMetadataException {
        map.clear();
        for (String key : tag.getKeys()) {
            Tag rawSubTag = tag.get(key);
            if (!(rawSubTag instanceof CompoundTag))
                throw new InvalidMetadataException("Subtag \"" + key + "\" is not a compound tag");
            CompoundTag subTag = (CompoundTag) rawSubTag;
            if (!subTag.contains("type", NbtType.STRING))
                throw new InvalidMetadataException("Subtag \"" + key + "\" is missing required attribute \"type\"");
            String valueTypeIdStr = subTag.getString("type");
            Identifier valueTypeId;
            try {
                valueTypeId = new Identifier(valueTypeIdStr);
            } catch (InvalidIdentifierException e) {
                throw new InvalidMetadataException("Subtag \"" + key + "\" has invalid value \"" + valueTypeIdStr
                        + "\" for attribute \"type\"", e);
            }
            //noinspection unchecked
            MetadataValueType<Object> valueType =
                    (MetadataValueType<Object>) Registries.METADATA_VALUE_TYPE.get(valueTypeId);
            if (valueType == null)
                throw new InvalidMetadataException("Subtag \"" + key + "\" specifies unregistered type \"" + valueTypeId + "\"");
            Object value;
            try {
                value = valueType.fromTag(subTag);
            } catch (InvalidMetadataException e) {
                throw new InvalidMetadataException("Subtag \"" + key + "\" failed to deserialize", e);
            }
            if (value != null)
                map.put(MetadataKey.of(key, valueType), value);
        }
    }

    public @NotNull CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        for (MetadataKey<Object> key : map.keySet()) {
            Object rawValue = map.get(key);
            if (!key.getValueClass().isInstance(rawValue))
                continue;
            Identifier valueTypeId = Registries.METADATA_VALUE_TYPE.getId(key.getValueType());
            if (valueTypeId == null)
                continue;
            CompoundTag subTag = key.getValueType().toTag(key.getValueClass().cast(rawValue));
            subTag.putString("type", valueTypeId.toString());
            tag.put(key.getId(), subTag);
        }
        return tag;
    }
}
