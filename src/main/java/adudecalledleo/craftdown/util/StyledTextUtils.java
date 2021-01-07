package adudecalledleo.craftdown.util;

import adudecalledleo.craftdown.mixin.KeybindTextAccessor;
import adudecalledleo.craftdown.mixin.TranslatableTextAccessor;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class StyledTextUtils {
    private StyledTextUtils() { }

    public interface Visitor {
        <T> @NotNull Optional<T> visit(Style style, String asString);
    }

    public static <T> @NotNull Optional<T> visitSelf(@NotNull Text text, @NotNull Visitor visitor, @NotNull Style baseStyle) {
        if (text instanceof TranslatableTextAccessor) {
            TranslatableTextAccessor tta = (TranslatableTextAccessor) text;
            tta.callUpdateTranslations();
            for (StringVisitable translation : tta.getTranslations()) {
                Optional<T> optional = visitor.visit(baseStyle, translation.getString());
                if (optional.isPresent())
                    return optional;
            }
            return Optional.empty();
        } else if (text instanceof KeybindTextAccessor)
            return visit(((KeybindTextAccessor) text).callGetTranslated(), visitor, baseStyle);
        else
            return visitor.visit(text.getStyle().withParent(baseStyle), text.asString());
    }

    public static <T> @NotNull Optional<T> visit(@NotNull Text text, @NotNull Visitor visitor, @NotNull Style baseStyle) {
        Optional<T> optional = visitSelf(text, visitor, baseStyle);
        if (!optional.isPresent()) {
            for (Text sibling : text.getSiblings()) {
                optional = visit(sibling, visitor, baseStyle);
                if (optional.isPresent())
                    return optional;
            }
        }
        return optional;
    }
}
