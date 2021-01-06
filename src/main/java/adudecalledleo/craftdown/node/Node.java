package adudecalledleo.craftdown.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static adudecalledleo.craftdown.Craftdown.LOGGER;

public abstract class Node {
    protected @Nullable Node parent;
    protected @NotNull List<Node> children;

    public Node() {
        parent = null;
        children = new ArrayList<>();
    }

    protected void setParent(@Nullable Node parent) {
        this.parent = parent;
    }

    public @Nullable Node getParent() {
        return parent;
    }
    public @NotNull List<Node> getChildren() {
        return children;
    }
    public boolean addChild(@NotNull Node node) {
        LOGGER.info("{}: adding node {}", this, node);
        node.unlink();
        node.setParent(this);
        return children.add(node);
    }
    public boolean removeChild(@NotNull Node node) {
        LOGGER.info("{}: removing node {}", this, node);
        node.setParent(null);
        return children.remove(node);
    }
    public boolean insertChild(int index, @NotNull Node node) {
        if (index < 0 || index > getChildCount())
            return false;
        LOGGER.info("{}: inserting node {} at {}", this, node, index);
        node.setParent(this);
        children.add(index, node);
        return true;
    }
    public boolean hasChild(@NotNull Node node) {
        return children.contains(node);
    }

    public int getChildCount() {
        return children.size();
    }
    public @Nullable Node getChildAt(int index) {
        if (index < 0 || index >= getChildCount())
            return null;
        return children.get(index);
    }
    public int getIndexOfChild(@NotNull Node child) {
        return children.indexOf(child);
    }

    public @Nullable Node getChildBefore(@NotNull Node child) {
        int pos = getIndexOfChild(child);
        if (pos < 1)
            return null;
        return children.get(pos - 1);
    }
    public @Nullable Node getChildAfter(@NotNull Node child) {
        int pos = getIndexOfChild(child);
        if (pos < 0 || pos == children.size())
            return null;
        return children.get(pos + 1);
    }

    public @Nullable Node getFirstSibling() {
        if (parent == null)
            return null;
        return parent.children.get(0);
    }
    public @Nullable Node getLastSibling() {
        if (parent == null)
            return null;
        return parent.children.get(parent.getChildCount() - 1);
    }
    
    public @Nullable Node getPreviousSibling() {
        if (parent == null)
            return null;
        return parent.getChildBefore(this);
    }
    public @Nullable Node getNextSibling() {
        if (parent == null)
            return null;
        return parent.getChildAfter(this);
    }

    public boolean addSiblingBefore(Node node) {
        if (parent == null)
            return false;
        int index = parent.getIndexOfChild(this);
        return parent.insertChild(index, node);
    }
    public boolean addSiblingAfter(Node node) {
        if (parent == null)
            return false;
        int index = parent.getIndexOfChild(this);
        return parent.insertChild(index + 1, node);
    }

    public void unlink() {
        if (parent == null)
            return;
        parent.removeChild(this);
    }

    public interface Visitor {
        void visit(@NotNull Node node);
    }

    public void visit(@NotNull Visitor visitor) {
        visitor.visit(this);
    }

    public void visitChildren(@NotNull Visitor visitor) {
        for (Node child : children)
            visitor.visit(child);
    }
}
