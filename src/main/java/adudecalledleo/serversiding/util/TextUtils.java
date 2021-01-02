package adudecalledleo.serversiding.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TextUtils {
    private TextUtils() { }

    /**
     * Ensures the text's "italic" flag is set to a value, so it won't be italicized automatically when used as an item name.
     */
    public static @NotNull MutableText ensureItalicSet(@NotNull MutableText text) {
        return text.styled(style -> style.withItalic(style.isItalic()));
    }

    public static @NotNull List<Text> toLore(@NotNull Text... lines) {
        return Stream.of(lines)
                .map(Text::shallowCopy)
                .map(line -> line
                        .styled(style -> style.withColor(Formatting.GRAY).withItalic(true)))
                .collect(Collectors.toList());
    }
}
