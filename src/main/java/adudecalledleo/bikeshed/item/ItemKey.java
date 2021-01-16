package adudecalledleo.bikeshed.item;

import adudecalledleo.bikeshed.tag.ImmutableCompoundTag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ItemKey {
    public static @NotNull ItemKey of(@NotNull Item item, @Nullable CompoundTag tag) {
        return new ItemKey(item, tag == null ? null : ImmutableCompoundTag.copyOf(tag));
    }

    public static @NotNull ItemKey of(@NotNull Item item) {
        return new ItemKey(item, null);
    }

    private final @NotNull Item item;
    private final @Nullable ImmutableCompoundTag tag;

    private ItemKey(@NotNull Item item, @Nullable ImmutableCompoundTag tag) {
        this.item = item;
        this.tag = tag;
    }

    public @NotNull Item getItem() {
        return item;
    }

    public @Nullable ImmutableCompoundTag getTag() {
        return tag;
    }

    public boolean matches(@NotNull ItemStack stack) {
        if (item != stack.getItem())
            return false;
        if (tag == null && !stack.hasTag())
            return true;
        if (tag == null && stack.hasTag() || tag != null && !stack.hasTag())
            return false;
        return tag != null && tag.equals(stack.getTag());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemKey other = (ItemKey) o;
        return item == other.item && Objects.equals(tag, other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }
}
