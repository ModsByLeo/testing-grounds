package adudecalledleo.craftdown.render;

import adudecalledleo.craftdown.node.*;
import org.jetbrains.annotations.NotNull;

public final class MarkdownRendererImpl implements MarkdownRenderer {
    private final String lineBreak;
    private final char italicDelimiter;

    public MarkdownRendererImpl(String lineBreak, char italicDelimiter) {
        this.lineBreak = lineBreak;
        this.italicDelimiter = italicDelimiter;
    }

    @Override
    public @NotNull String render(@NotNull Node node) {
        NodeRenderer nodeRenderer = new NodeRenderer();
        nodeRenderer.visit(node);
        return nodeRenderer.getResult();
    }

    private final class NodeRenderer {
        public final NodeVisitor nodeVisitor;
        private final StringBuilder sb;

        public NodeRenderer() {
            nodeVisitor = new NodeVisitor(this::visit);
            sb = new StringBuilder();
        }

        private void visit(@NotNull Node node) {
            if (node instanceof TextNode)
                sb.append(((TextNode) node).getContents());
            else if (node instanceof LineBreakNode)
                sb.append(lineBreak);
            else if (node instanceof StyleNode) {
                StyleNode.Type type = ((StyleNode) node).getType();
                String delimiter;
                switch (type) {
                case BOLD:
                    delimiter = "**";
                    break;
                case ITALIC:
                    delimiter = italicDelimiter + "";
                    break;
                case UNDERLINE:
                    delimiter = "__";
                    break;
                case STRIKETHROUGH:
                    delimiter = "~~";
                    break;
                default:
                    throw new RuntimeException("Unsupported style type " + type);
                }
                sb.append(delimiter);
                nodeVisitor.visitChildren(node);
                sb.append(delimiter);
            } else if (node instanceof LinkNode) {
                sb.append('[');
                nodeVisitor.visit(node);
                sb.append("](").append(((LinkNode) node).getUrl().toString()).append(')');
            } else
                throw new RuntimeException("Unsupported node type " + node.getClass());
        }

        public String getResult() {
            return sb.toString();
        }
    }
}
