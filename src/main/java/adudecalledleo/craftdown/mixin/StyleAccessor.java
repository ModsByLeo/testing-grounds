package adudecalledleo.craftdown.mixin;

import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * For some reason, {@link Style#withUnderline(Boolean)} is only available on clients.
 * Therefore, we need this thing.
 */
@Mixin(Style.class)
public interface StyleAccessor {
    @Accessor void setUnderlined(@Nullable Boolean underline);
}
