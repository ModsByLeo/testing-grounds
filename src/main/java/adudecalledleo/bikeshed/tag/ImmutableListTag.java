package adudecalledleo.bikeshed.tag;

import adudecalledleo.bikeshed.mixin.ListTagAccessor;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImmutableListTag extends ListTag {
    public static @NotNull ImmutableListTag copyOf(@NotNull ListTag original) {
        ImmutableList.Builder<Tag> listBuilder = ImmutableList.builder();
        for (Tag value : original) {
            if (value == null)
                // ???
                continue;
            listBuilder.add(TagUtils.immutableCopyOf(value));
        }
        return new ImmutableListTag(listBuilder.build(), false);
    }

    protected ImmutableListTag(List<Tag> list, boolean makeImmutableCopy) {
        super();
        // thanks :mojank:
        ((ListTagAccessor) this).setValue(makeImmutableCopy ? ImmutableList.copyOf(list) : list);
    }

    public ImmutableListTag(List<Tag> list) {
        this(list, true);
    }
}
