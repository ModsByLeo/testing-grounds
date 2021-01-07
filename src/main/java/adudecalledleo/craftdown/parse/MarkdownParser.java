package adudecalledleo.craftdown.parse;

import adudecalledleo.craftdown.node.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public interface MarkdownParser {
    @NotNull Node parse(@NotNull String str);

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private boolean parseLinks;
        private @Nullable URL linkContext;

        private Builder() {
            parseLinks = false;
            linkContext = null;
        }

        public @NotNull Builder parseLinks(boolean parseLinks) {
            this.parseLinks = parseLinks;
            return this;
        }

        public @NotNull Builder linkContext(@Nullable URL linkContext) {
            this.linkContext = linkContext;
            return this;
        }

        public @NotNull MarkdownParser build() {
            return new MarkdownParserImpl(parseLinks, linkContext);
        }
    }
}
