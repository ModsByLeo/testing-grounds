package adudecalledleo.bikeshed.tag;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImmutableByteArrayTag extends ByteArrayTag {
    public static @NotNull ImmutableByteArrayTag copyOf(@NotNull ByteArrayTag original) {
        return new ImmutableByteArrayTag(original.getByteArray());
    }

    public ImmutableByteArrayTag(byte[] value) {
        super(value);
    }

    public ImmutableByteArrayTag(List<Byte> value) {
        super(value);
    }

    @Override
    public byte[] getByteArray() {
        return super.getByteArray().clone();
    }

    @Deprecated
    @Override
    public ByteTag set(int i, ByteTag byteTag) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public ByteTag method_10536(int i) {
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
    public void method_10531(int i, ByteTag byteTag) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public ByteTag remove(int i) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
