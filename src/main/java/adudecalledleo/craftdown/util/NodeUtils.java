package adudecalledleo.craftdown.util;

import adudecalledleo.craftdown.node.Node;
import adudecalledleo.craftdown.node.TextNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class NodeUtils {
    private NodeUtils() { }

    public static void mergeTextNodes(@NotNull Node root) {
        List<TextNode> toMerge = new ArrayList<>();
        List<Node> newChildren = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            Node node = root.getChildAt(i);
            if (node instanceof TextNode) {
                if (!((TextNode) node).getContents().isEmpty())
                // queue node for mergin'
                    toMerge.add((TextNode) node);
            } else if (node != null) {
                TextNode mergedNode = mergeTextNodes(toMerge);
                if (!mergedNode.getContents().isEmpty())
                    newChildren.add(mergedNode);
                toMerge.clear();
                // keep this non-text node child
                newChildren.add(node);
                // let's get recursive
                mergeTextNodes(node);
            }
        }
        if (!toMerge.isEmpty()) {
            if (toMerge.size() == 1) {
                TextNode node = toMerge.get(0);
                if (!node.getContents().isEmpty())
                    newChildren.add(node);
            } else {
                TextNode mergedNode = mergeTextNodes(toMerge);
                if (!mergedNode.getContents().isEmpty())
                    newChildren.add(mergedNode);
            }
            toMerge.clear();
        }
        // unlink all children
        List<Node> toUnlink = new ArrayList<>(root.getChildren());
        for (Node node : toUnlink)
            node.unlink();
        // add new children (and old children that got unlinked)
        for (Node node : newChildren)
            root.addChild(node);
    }

    public static @NotNull TextNode mergeTextNodes(@NotNull List<TextNode> nodes) {
        return new TextNode(nodes.stream().map(TextNode::getContents).collect(Collectors.joining()));
    }
}
