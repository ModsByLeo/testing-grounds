package adudecalledleo.craftdown.node;

public final class LineBreakNode extends Node {
    public static final Node INSTANCE = new LineBreakNode();

    private LineBreakNode() {
        super();
    }

    @Override
    public String toString() {
        return "LineBreakNode{}";
    }
}
