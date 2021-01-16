package adudecalledleo.bikeshed.tag;

import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

public final class TagUtils {
    public static @NotNull Tag immutableCopyOf(@NotNull Tag original) {
        if (original.getReader().isImmutable())
            // already immutable!
            return original;
        if (original instanceof CompoundTag)
            return ImmutableCompoundTag.copyOf((CompoundTag) original);
        if (original instanceof ListTag)
            return ImmutableListTag.copyOf((ListTag) original);
        if (original instanceof ByteArrayTag)
            return ImmutableByteArrayTag.copyOf((ByteArrayTag) original);
        if (original instanceof IntArrayTag)
            return ImmutableIntArrayTag.copyOf((IntArrayTag) original);
        if (original instanceof LongArrayTag)
            return ImmutableLongArrayTag.copyOf((LongArrayTag) original);
        throw new UnsupportedOperationException("Tag of type " + original.getClass().getName() + " cannot be copied as immutable!");
    }
}
