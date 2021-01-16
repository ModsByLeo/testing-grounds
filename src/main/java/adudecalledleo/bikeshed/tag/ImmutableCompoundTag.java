package adudecalledleo.bikeshed.tag;

import com.google.common.collect.ImmutableMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ImmutableCompoundTag extends CompoundTag {
    public static @NotNull ImmutableCompoundTag copyOf(@NotNull CompoundTag original) {
        ImmutableMap.Builder<String, Tag> mapBuilder = ImmutableMap.builder();
        for (String key : original.getKeys()) {
            Tag value = original.get(key);
            if (value == null)
                // ???
                continue;
            mapBuilder.put(key, TagUtils.immutableCopyOf(value));
        }
        return new ImmutableCompoundTag(mapBuilder.build(), false);
    }

    protected ImmutableCompoundTag(Map<String, Tag> tags, boolean makeImmutableCopy) {
        super(makeImmutableCopy ? ImmutableMap.copyOf(tags) : tags);
    }

    public ImmutableCompoundTag(Map<String, Tag> tags) {
        this(tags, true);
    }
}
