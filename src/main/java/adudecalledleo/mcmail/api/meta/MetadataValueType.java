package adudecalledleo.mcmail.api.meta;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MetadataValueType<V> {
    @NotNull Class<V> getValueClass();
    @Nullable V fromTag(@NotNull CompoundTag tag) throws InvalidMetadataException;
    @NotNull CompoundTag toTag(@Nullable V value);
}
