package adudecalledleo.craftdown.node;

import org.jetbrains.annotations.Nullable;

public final class Document extends Node {
    @Override
    public void setParent(@Nullable Node parent) {
        throw new RuntimeException("Document node can only be root node!");
    }

    @Override
    public String toString() {
        return "Document{}";
    }
}
