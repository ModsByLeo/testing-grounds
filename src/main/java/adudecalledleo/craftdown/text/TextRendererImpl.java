package adudecalledleo.craftdown.text;

import adudecalledleo.craftdown.node.*;
import adudecalledleo.craftdown.util.StyleUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

final class TextRendererImpl implements TextRenderer {
    @Override
    public @NotNull List<Text> render(@NotNull Node root) {
        NodeRenderer nodeRenderer = new NodeRenderer();
        nodeRenderer.nodeVisitor.visit(root);
        nodeRenderer.flush();
        return nodeRenderer.getLines();
    }

    private static final class NodeRenderer {
        public final NodeVisitor nodeVisitor;
        private final StyleStack styleStack;
        private final ImmutableList.Builder<Text> builder;
        private MutableText currentText;

        public NodeRenderer() {
            nodeVisitor = new NodeVisitor(this::visit);
            styleStack = new StyleStack();
            builder = ImmutableList.builder();
            styleStack.push(StyleUtils.withStrikethrough(StyleUtils.withUnderline(Style.EMPTY.withBold(false).withItalic(false),
                    false), false));
            currentText = new LiteralText("").setStyle(styleStack.peek());
        }

        private void visit(@NotNull Node node) {
            if (node instanceof TextNode)
                currentText.append(new LiteralText(((TextNode) node).getContents()).setStyle(styleStack.peek()));
            else if (node instanceof LineBreakNode)
                flush();
            else if (node instanceof StyleNode) {
                Style style = styleStack.peek();
                StyleNode.Type type = ((StyleNode) node).getType();
                switch (type) {
                case BOLD:
                    style = style.withBold(true);
                    break;
                case ITALIC:
                    style = style.withItalic(true);
                    break;
                case UNDERLINE:
                    style = StyleUtils.withUnderline(style, true);
                    break;
                case STRIKETHROUGH:
                    style = StyleUtils.withStrikethrough(style, true);
                    break;
                default:
                    throw new RuntimeException("Unsupported style type " + type);
                }
                styleStack.push(style);
                nodeVisitor.visitChildren(node);
                styleStack.pop();
            } else if (node instanceof LinkNode) {
                Style style = styleStack.peek().withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_URL, ((LinkNode) node).getUrl().toString()));
                styleStack.push(style);
                nodeVisitor.visitChildren(node);
                styleStack.pop();
            } else
                throw new RuntimeException("Unsupported node type " + node.getClass());
        }

        public void flush() {
            builder.add(currentText);
            currentText = new LiteralText("").setStyle(styleStack.peek());
        }

        public List<Text> getLines() {
            return builder.build();
        }
    }

    private static final class StyleStack {
        private static final class Entry {
            private final Entry next;
            private final Style value;

            private Entry(Entry next, Style value) {
                this.next = next;
                this.value = value;
            }
        }

        private Entry head;

        public StyleStack() {
            head = null;
        }

        public boolean isEmpty() {
            return head == null;
        }

        public void push(Style style) {
            head = new Entry(head, style);
        }

        public Style peek() {
            return head.value;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Style pop() {
            Style style = head.value;
            head = head.next;
            return style;
        }
    }
}
