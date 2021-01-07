package adudecalledleo.craftdown.render;

import adudecalledleo.craftdown.node.Node;
import org.jetbrains.annotations.NotNull;

public interface MarkdownRenderer {
    @NotNull String render(@NotNull Node node);

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private @NotNull String lineBreak;
        private char italicDelimiter;

        private Builder() {
            lineBreak = "\n";
            italicDelimiter = '*';
        }

        public @NotNull Builder lineBreak(@NotNull String lineBreak) {
            this.lineBreak = lineBreak;
            return this;
        }

        public @NotNull Builder italicDelimiter(char italicDelimiter) {
            this.italicDelimiter = italicDelimiter;
            return this;
        }

        public @NotNull MarkdownRenderer build() {
            return new MarkdownRendererImpl(lineBreak, italicDelimiter);
        }
    }
}
