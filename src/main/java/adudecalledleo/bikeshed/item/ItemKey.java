package adudecalledleo.bikeshed.item;

import adudecalledleo.bikeshed.tag.ImmutableCompoundTag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ItemKey {
    public static final @NotNull ItemKey EMPTY = of(Items.AIR);

    public static @NotNull ItemKey of(@NotNull Item item, @Nullable CompoundTag tag) {
        return new ItemKey(item, tag == null ? null : ImmutableCompoundTag.copyOf(tag));
    }

    public static @NotNull ItemKey of(@NotNull Item item) {
        return new ItemKey(item, null);
    }

    public static @NotNull ItemKey of(@NotNull ItemConvertible itemConvertible, @Nullable CompoundTag tag) {
        return of(itemConvertible.asItem(), tag);
    }

    public static @NotNull ItemKey of(@NotNull ItemConvertible itemConvertible) {
        return of(itemConvertible.asItem());
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

    public boolean hasTag() {
        return tag != null;
    }

    public @Nullable ImmutableCompoundTag getTag() {
        return tag;
    }

    public @Nullable CompoundTag copyTag() {
        if (tag == null)
            return null;
        return tag.copy();
    }

    public boolean isEmpty() {
        return this == EMPTY || item == Items.AIR;
    }

    public boolean matches(@NotNull ItemStack stack) {
        if (stack.isEmpty())
            return isEmpty();
        if (item != stack.getItem())
            return false;
        if (stack.hasTag())
            return tag != null && tag.equals(stack.getTag());
        else
            return tag == null;
    }

    public @NotNull ItemStack toStack(int count) {
        if (isEmpty())
            return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item, count);
        stack.setTag(copyTag());
        return stack;
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
