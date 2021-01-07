package adudecalledleo.craftdown.render;

import adudecalledleo.craftdown.node.Node;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;
import java.util.function.BiFunction;

public interface TextRenderer {
    @NotNull List<Text> render(@NotNull Node root);

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private @NotNull BiFunction<URL, Style, Style> linkStyleTransformer;

        private Builder() {
            linkStyleTransformer = (url, style) -> style;
        }

        public @NotNull Builder linkStyleTransformer(@NotNull BiFunction<URL, Style, Style> linkStyleTransformer) {
            this.linkStyleTransformer = linkStyleTransformer;
            return this;
        }

        public @NotNull TextRenderer build() {
            return new TextRendererImpl(linkStyleTransformer);
        }
    }
}
