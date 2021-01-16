package adudecalledleo.bikeshed.mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ListTag.class)
public interface ListTagAccessor {
    @Accessor void setValue(List<Tag> value);
}
