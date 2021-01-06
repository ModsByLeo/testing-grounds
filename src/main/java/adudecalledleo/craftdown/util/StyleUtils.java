package adudecalledleo.craftdown.util;

import adudecalledleo.craftdown.mixin.StyleAccessor;
import net.minecraft.text.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StyleUtils {
    public static final Style ALL_OFF = StyleUtils.withStrikethrough(
            StyleUtils.withUnderline(Style.EMPTY
                            .withBold(false)
                            .withItalic(false),
                    false),
            false);

    public static @NotNull Style copy(@NotNull Style style) {
        return style.withColor(style.getColor());
    }

    public static @NotNull Style withUnderline(@NotNull Style style, @Nullable Boolean underline) {
        style = copy(style);
        ((StyleAccessor) style).setUnderlined(underline);
        return style;
    }

    public static @NotNull Style withStrikethrough(@NotNull Style style, @Nullable Boolean strikethrough) {
        style = copy(style);
        ((StyleAccessor) style).setStrikethrough(strikethrough);
        return style;
    }
}
