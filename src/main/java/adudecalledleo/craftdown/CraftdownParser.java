package adudecalledleo.craftdown;

import adudecalledleo.craftdown.node.Node;
import adudecalledleo.craftdown.parser.MarkdownParser;
import org.jetbrains.annotations.NotNull;

public final class CraftdownParser {
    private CraftdownParser() { }

    public static @NotNull Node fromMarkdownString(@NotNull String src) {
        return MarkdownParser.parse(src);
    }
}
