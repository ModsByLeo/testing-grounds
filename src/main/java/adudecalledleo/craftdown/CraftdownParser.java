package adudecalledleo.craftdown;

import adudecalledleo.craftdown.impl.CraftdownParserImpl;
import adudecalledleo.craftdown.node.Node;
import org.jetbrains.annotations.NotNull;

public interface CraftdownParser {
    @NotNull Node parse(@NotNull String str);

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private boolean parseLinks;

        private Builder() {
            parseLinks = false;
        }

        public @NotNull Builder parseLinks(boolean parseLinks) {
            this.parseLinks = parseLinks;
            return this;
        }

        public @NotNull CraftdownParser build() {
            return new CraftdownParserImpl(parseLinks);
        }
    }
}
