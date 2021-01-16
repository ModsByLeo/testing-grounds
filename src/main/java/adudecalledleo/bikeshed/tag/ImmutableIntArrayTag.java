package adudecalledleo.bikeshed.tag;

import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImmutableIntArrayTag extends IntArrayTag {
    public static @NotNull ImmutableIntArrayTag copyOf(@NotNull IntArrayTag original) {
        return new ImmutableIntArrayTag(original.getIntArray());
    }

    public ImmutableIntArrayTag(int[] value) {
        super(value.clone());
    }

    public ImmutableIntArrayTag(List<Integer> value) {
        super(value);
    }

    @Override
    public int[] getIntArray() {
        return super.getIntArray().clone();
    }

    @Deprecated
    @Override
    public IntTag set(int i, IntTag intTag) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean add(IntTag intTag) {
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
    public IntTag remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
