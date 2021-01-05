package adudecalledleo.craftdown.node;

import org.jetbrains.annotations.NotNull;

public final class StyleNode extends Node {
    public enum Type {
        BOLD, ITALIC, UNDERLINE, STRIKETHROUGH
    }

    private final @NotNull Type type;

    public StyleNode(@NotNull Type type) {
        this.type = type;
    }

    public @NotNull Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "StyleNode{" +
                "type=" + type +
                '}';
    }
}
