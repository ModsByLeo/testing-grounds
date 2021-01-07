package adudecalledleo.craftdown.util;

import adudecalledleo.craftdown.node.*;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class NodeUtils {
    private NodeUtils() { }

    public static void mergeNodes(@NotNull Node root) {
        NodeMerger nodeMerger = new NodeMerger();
        nodeMerger.nodeVisitor.visitChildren(root);
        nodeMerger.flush();
        ArrayList<Node> toUnlink = new ArrayList<>(root.getChildren());
        root.removeChildren(toUnlink);
        root.addChildren(nodeMerger.newChildren);
    }

    private static final class NodeMerger {
        public final NodeVisitor nodeVisitor;
        private final ArrayList<TextNode> textsToMerge;
        private final ArrayList<Node> stylesToMerge;
        private StyleNode.Type styleTypeToMerge;
        private final ArrayList<Node> linksToMerge;
        private URL linkUrlToMerge;
        private final ArrayList<Node> newChildren;

        public NodeMerger() {
            nodeVisitor = new NodeVisitor(this::visit);
            textsToMerge = new ArrayList<>();
            stylesToMerge = new ArrayList<>();
            styleTypeToMerge = null;
            linksToMerge = new ArrayList<>();
            linkUrlToMerge = null;
            newChildren = new ArrayList<>();
        }

        private void visit(@NotNull Node node) {
            if (node instanceof LineBreakNode) {
                flush();
                newChildren.add(node);
            } else if (node instanceof TextNode) {
                if (!((TextNode) node).getContents().isEmpty()) {
                    mergeStyleNodes();
                    mergeLinkNodes();
                    textsToMerge.add((TextNode) node);
                }
            } else if (node.hasChildren()) {
                if (node instanceof StyleNode) {
                    mergeTextNodes();
                    mergeLinkNodes();
                    StyleNode.Type styleType = ((StyleNode) node).getType();
                    if (styleTypeToMerge != null && styleType != styleTypeToMerge)
                        mergeStyleNodes();
                    styleTypeToMerge = styleType;
                    stylesToMerge.add(node);
                } else if (node instanceof LinkNode) {
                    mergeTextNodes();
                    mergeStyleNodes();
                    URL url = ((LinkNode) node).getUrl();
                    // in case you're wondering why this is comparing their string representations,
                    // URL.equals performs a name lookup for some asinine reason
                    if (linkUrlToMerge != null && linkUrlToMerge.toString().equals(url.toString()))
                        mergeLinkNodes();
                    linkUrlToMerge = url;
                    linksToMerge.add(node);
                } else {
                    // default to merging children and keeping
                    NodeUtils.mergeNodes(node);
                    newChildren.add(node);
                }
            }
        }

        private void mergeTextNodes() {
            if (textsToMerge.isEmpty())
                return;
            newChildren.add(new TextNode(textsToMerge.stream().map(TextNode::getContents).collect(Collectors.joining())));
            textsToMerge.clear();
        }

        private void transferChildren(Node from, Node to) {
            ArrayList<Node> children = new ArrayList<>(from.getChildren());
            for (Node child : children)
                to.addChild(child);
        }

        private void mergeNodes(ArrayList<Node> toMerge, Node newRoot) {
            if (toMerge.isEmpty())
                return;
            newChildren.add(newRoot);
            for (Node node : toMerge)
                transferChildren(node, newRoot);
            toMerge.clear();
            NodeUtils.mergeNodes(newRoot);
        }

        private void mergeStyleNodes() {
            if (styleTypeToMerge == null)
                return;
            mergeNodes(stylesToMerge, new StyleNode(styleTypeToMerge));
            styleTypeToMerge = null;
        }

        private void mergeLinkNodes() {
            if (linkUrlToMerge == null)
                return;
            mergeNodes(linksToMerge, new LinkNode(linkUrlToMerge));
            linkUrlToMerge = null;
        }

        public void flush() {
            mergeTextNodes();
            mergeStyleNodes();
            mergeLinkNodes();
        }
    }
}
