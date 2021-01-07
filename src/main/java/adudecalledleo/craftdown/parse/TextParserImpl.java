package adudecalledleo.craftdown.parse;

import adudecalledleo.craftdown.node.*;
import adudecalledleo.craftdown.util.NodeUtils;
import adudecalledleo.craftdown.util.StyledTextUtils;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

final class TextParserImpl implements TextParser {
    private final URL linkContext;

    public TextParserImpl(URL linkContext) {
        this.linkContext = linkContext;
    }

    @Override
    public @NotNull Node parse(@NotNull List<Text> texts, @NotNull Style baseStyle) {
        TextVisitor visitor = new TextVisitor();
        final int textCount = texts.size();
        for (int i = 0; i < textCount; i++) {
            StyledTextUtils.visit(texts.get(i), visitor, baseStyle);
            if (i < textCount - 1)
                visitor.trueRoot.addChild(new LineBreakNode());
        }
        NodeUtils.mergeNodes(visitor.trueRoot);
        return visitor.trueRoot;
    }

    private final class TextVisitor implements StyledTextUtils.Visitor {
        public final Document trueRoot;

        public TextVisitor() {
            trueRoot = new Document();
        }

        @Override
        public @NotNull <T> Optional<T> visit(Style style, String asString) {
            Node root = trueRoot;
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
                URL url = null;
                try {
                    url = new URL(linkContext, clickEvent.getValue());
                } catch (MalformedURLException ignored) { }
                if (url != null) {
                    Node newRoot = new LinkNode(url);
                    root.addChild(newRoot);
                    root = newRoot;
                }
            }
            if (style.isBold()) {
                Node newRoot = new StyleNode(StyleNode.Type.BOLD);
                root.addChild(newRoot);
                root = newRoot;
            }
            if (style.isItalic()) {
                Node newRoot = new StyleNode(StyleNode.Type.ITALIC);
                root.addChild(newRoot);
                root = newRoot;
            }
            if (style.isUnderlined()) {
                Node newRoot = new StyleNode(StyleNode.Type.UNDERLINE);
                root.addChild(newRoot);
                root = newRoot;
            }
            if (style.isStrikethrough()) {
                Node newRoot = new StyleNode(StyleNode.Type.STRIKETHROUGH);
                root.addChild(newRoot);
                root = newRoot;
            }
            root.addChild(new TextNode(asString));
            return Optional.empty();
        }
    }
}
