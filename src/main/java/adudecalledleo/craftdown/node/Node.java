package adudecalledleo.craftdown.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Node {
    protected @Nullable Node parent;
    protected @NotNull List<Node> children;
    private final @NotNull List<Node> childrenUnmodView;

    public Node() {
        parent = null;
        children = new ArrayList<>();
        childrenUnmodView = Collections.unmodifiableList(children);
    }

    protected void setParent(@Nullable Node parent) {
        this.parent = parent;
    }

    public @Nullable Node getParent() {
        return parent;
    }
    public @NotNull List<Node> getChildren() {
        return childrenUnmodView;
    }
    public boolean addChild(@NotNull Node node) {
        children.add(node);
        node.unlink();
        node.setParent(this);
        return true;
    }
    public boolean removeChild(@NotNull Node node) {
        if (children.remove(node)) {
            node.setParent(null);
            return true;
        }
        return false;
    }
    public boolean insertChild(int index, @NotNull Node node) {
        if (index < 0 || index > getChildCount())
            return false;
        children.add(index, node);
        node.setParent(this);
        return true;
    }
    public boolean hasChild(@NotNull Node node) {
        return children.contains(node);
    }

    public boolean addChildren(@NotNull Collection<Node> nodes) {
        boolean ret = false;
        for (Node node : nodes)
            ret |= addChild(node);
        return ret;
    }
    public boolean removeChildren(@NotNull Collection<Node> nodes) {
        boolean ret = false;
        for (Node node : nodes)
            ret |= removeChild(node);
        return ret;
    }
    public boolean hasChildren(@NotNull Collection<Node> nodes) {
        return children.containsAll(nodes);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
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
