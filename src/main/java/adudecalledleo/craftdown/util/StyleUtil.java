package adudecalledleo.craftdown.util;

import adudecalledleo.craftdown.mixin.StyleAccessor;
import net.minecraft.text.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StyleUtil {
    public static @NotNull Style copy(@NotNull Style style) {
        return style.withColor(style.getColor());
    }

    public static @NotNull Style withUnderline(@NotNull Style style, @Nullable Boolean underline) {
        style = copy(style);
        ((StyleAccessor) style).setUnderlined(underline);
        return style;
    }
}
