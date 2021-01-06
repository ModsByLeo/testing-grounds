package adudecalledleo.craftdown.util;

import adudecalledleo.craftdown.node.Node;
import adudecalledleo.craftdown.node.TextNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static adudecalledleo.craftdown.Craftdown.LOGGER;

public final class NodeUtils {
    private NodeUtils() { }

    public static void mergeTextNodes(@NotNull Node root) {
        LOGGER.info("mergeTextBoxes: {} START", root);
        List<TextNode> toMerge = new ArrayList<>();
        List<Node> newChildren = new ArrayList<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            Node node = root.getChildAt(i);
            if (node instanceof TextNode) {
                LOGGER.info("mergeTextNodes: Adding node {} to merge queue", node);
                // queue node for mergin'
                toMerge.add((TextNode) node);
            } else if (node != null) {
                TextNode mergedNode = mergeTextNodes(toMerge);
                LOGGER.info("mergeTextNodes: Merged queued nodes, got {}", mergedNode);
                if (mergedNode.getContents().isEmpty())
                    LOGGER.info("mergeTextNodes: Not adding empty node");
                else
                    newChildren.add(mergedNode);
                toMerge.clear();
                // keep this non-text node child
                LOGGER.info("mergeTextNodes: Keeping node {}", node);
                newChildren.add(node);
                // let's get recursive
                mergeTextNodes(node);
            }
        }
        if (!toMerge.isEmpty()) {
            if (toMerge.size() == 1) {
                TextNode node = toMerge.get(0);
                if (node.getContents().isEmpty())
                    LOGGER.info("mergeTextBoxes: Removing empty node {}", node);
                else {
                    LOGGER.info("mergeTextBoxes: Keeping node {}", node);
                    newChildren.add(node);
                }
            } else {
                LOGGER.info("mergeTextNodes: Merging last set of nodes");
                TextNode mergedNode = mergeTextNodes(toMerge);
                LOGGER.info("mergeTextNodes: Merged queued nodes, got {}", mergedNode);
                if (mergedNode.getContents().isEmpty())
                    LOGGER.info("mergeTextNodes: Not adding empty node");
                else
                    newChildren.add(mergedNode);
            }
            toMerge.clear();
        }
        // unlink all children
        List<Node> toUnlink = new ArrayList<>(root.getChildren());
        for (Node node : toUnlink) {
            LOGGER.info("mergeTextBoxes: Unlinking node {}", node);
            node.unlink();
        }
        // add new children (and old children that got unlinked)
        for (Node node : newChildren) {
            LOGGER.info("mergeTextBoxes: Adding node {}", node);
            root.addChild(node);
        }
        LOGGER.info("mergeTextBoxes: {} END", root);
    }

    public static @NotNull TextNode mergeTextNodes(@NotNull List<TextNode> nodes) {
        return new TextNode(nodes.stream().map(TextNode::getContents).collect(Collectors.joining()));
    }
}
