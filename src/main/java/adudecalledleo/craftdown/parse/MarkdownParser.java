package adudecalledleo.craftdown.parse;

import adudecalledleo.craftdown.node.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public interface MarkdownParser {
    @NotNull Node parse(@NotNull String source);

    static @NotNull Builder builder() {
        return new Builder();
    }

    final class Builder {
        private boolean specialUnderscoreHandling;
        private boolean parseLinks;
        private @Nullable URL linkContext;

        private Builder() {
            specialUnderscoreHandling = true;
            parseLinks = false;
            linkContext = null;
        }

        public @NotNull Builder specialUnderscoreHandling(boolean specialUnderscoreHandling) {
            this.specialUnderscoreHandling = specialUnderscoreHandling;
            return this;
        }

        public @NotNull Builder parseLinks(boolean parseLinks) {
            this.parseLinks = parseLinks;
            return this;
        }

        public @NotNull Builder linkContext(@Nullable URL linkContext) {
            this.linkContext = linkContext;
            return this;
        }

        public @NotNull Builder configureForDiscord() {
            specialUnderscoreHandling = false; // Discord is noncompliant, apparently
            parseLinks = false; // 99% sure only bots are allowed to use this
            linkContext = null;
            return this;
        }

        public @NotNull MarkdownParser build() {
            return new MarkdownParserImpl(specialUnderscoreHandling, parseLinks, linkContext);
        }
    }
}
