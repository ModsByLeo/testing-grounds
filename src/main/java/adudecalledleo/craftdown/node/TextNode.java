package adudecalledleo.craftdown.node;

import org.jetbrains.annotations.NotNull;

public final class TextNode extends Node {
    private final @NotNull String contents;

    public TextNode(@NotNull String contents) {
        this.contents = contents;
    }

    public @NotNull String getContents() {
        return contents;
    }

    @Override
    public String toString() {
        return "TextNode{" +
                "contents='" + contents + '\'' +
                '}';
    }
}
