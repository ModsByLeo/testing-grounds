package adudecalledleo.craftdown.parse;

import adudecalledleo.craftdown.node.Node;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public interface TextParser {
    @NotNull Node parse(@NotNull List<Text> texts, @NotNull Style baseStyle);

    default @NotNull Node parse(@NotNull Text text, @NotNull Style baseStyle) {
        return parse(Collections.singletonList(text), baseStyle);
    }

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private @Nullable URL linkContext;

        private Builder() {
            linkContext = null;
        }

        public @NotNull Builder linkContext(@Nullable URL linkContext) {
            this.linkContext = linkContext;
            return this;
        }

        public @NotNull TextParser build() {
            return new TextParserImpl(linkContext);
        }
    }
}
