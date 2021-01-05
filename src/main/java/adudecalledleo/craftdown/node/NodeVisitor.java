package adudecalledleo.craftdown.node;

import org.jetbrains.annotations.NotNull;

public final class NodeVisitor implements Node.Visitor {
    private final @NotNull Node.Visitor delegate;

    public NodeVisitor(Node.@NotNull Visitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visit(@NotNull Node node) {
        delegate.visit(node);
    }

    public void visitChildren(@NotNull Node node) {
        node.visitChildren(this);
    }
}
