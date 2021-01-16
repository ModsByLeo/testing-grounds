package adudecalledleo.bikeshed.tag;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImmutableLongArrayTag extends LongArrayTag {
    public static @NotNull ImmutableLongArrayTag copyOf(@NotNull LongArrayTag original) {
        return new ImmutableLongArrayTag(original.getLongArray());
    }

    public ImmutableLongArrayTag(long[] value) {
        super(value);
    }

    public ImmutableLongArrayTag(LongSet value) {
        super(value);
    }

    public ImmutableLongArrayTag(List<Long> value) {
        super(value);
    }

    @Override
    public long[] getLongArray() {
        return super.getLongArray().clone();
    }

    @Deprecated
    @Override
    public LongTag method_10606(int i, LongTag longTag) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean add(LongTag longTag) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean setTag(int index, Tag tag) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean addTag(int index, Tag tag) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public LongTag remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
