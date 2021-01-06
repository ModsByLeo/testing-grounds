package adudecalledleo.craftdown;

import adudecalledleo.craftdown.impl.CraftdownParserImpl;
import adudecalledleo.craftdown.node.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public interface CraftdownParser {
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

        public @NotNull CraftdownParser build() {
            return new CraftdownParserImpl(parseLinks, linkContext);
        }
    }
}
