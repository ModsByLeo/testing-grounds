package adudecalledleo.craftdown.mixin;

import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Style.class)
public interface StyleAccessor {
    // Style.withUnderline is only available on clients
    @Accessor void setUnderlined(@Nullable Boolean underline);
    // this one just doesn't have a with* method lol
    @Accessor void setStrikethrough(@Nullable Boolean strikethrough);
}
