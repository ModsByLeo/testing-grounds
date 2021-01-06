package adudecalledleo.craftdown.markdown;

import adudecalledleo.craftdown.node.*;
import adudecalledleo.craftdown.util.CharUtils;
import adudecalledleo.craftdown.util.NodeUtils;
import adudecalledleo.craftdown.util.Scanner;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

final class MarkdownParserImpl implements MarkdownParser {
    private final boolean parseLinks;
    private final URL linkContext;

    public MarkdownParserImpl(boolean parseLinks, URL linkContext) {
        this.parseLinks = parseLinks;
        this.linkContext = linkContext;
    }

    @Override
    public @NotNull Node parse(@NotNull String src) {
        Document root = new Document();
        parseInternal(root, new Scanner(src));
        NodeUtils.mergeTextNodes(root);
        return root;
    }

    private void parseInternal(Node root, Scanner scanner) {
        final StringBuilder sb = new StringBuilder();
        char c;
        while ((c = scanner.peek()) != Scanner.END) {
            Node curLast = null;
            if (root.getChildCount() > 0)
                curLast = root.getChildAt(root.getChildCount() - 1);
            if (handleChar(root, scanner, c)) {
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\\')
                    sb.deleteCharAt(sb.length() - 1);
                Node textNode = new TextNode(sb.toString());
                sb.setLength(0);
                if (curLast == null)
                    root.insertChild(0, textNode);
                else
                    root.insertChild(root.getIndexOfChild(curLast) + 1, textNode);
                continue;
            }
            scanner.next();
            if (c == '\n') {
                root.addChild(new TextNode(sb.toString()));
                sb.setLength(0);
                root.addChild(new LineBreakNode());
                continue;
            }
            sb.append(c);
        }
        if (sb.length() > 0)
            root.addChild(new TextNode(sb.toString()));
    }

    private boolean handleChar(Node root, Scanner scanner, char c) {
        final char cp = scanner.peekPrevious();
        char cn;
        switch (c) {
        case '*':
            scanner.next();
            cn = scanner.peek();
            if (cn == '*')
                return handleStyleDouble(root, scanner, '*', cp, StyleNode.Type.BOLD);
            else
                return handleStyleSingle(root, scanner, '*', cp, StyleNode.Type.ITALIC);
        case '_':
            scanner.next();
            cn = scanner.peek();
            if (cn == '_')
                return handleStyleDouble(root, scanner, '_', cp, StyleNode.Type.UNDERLINE);
            else
                return handleStyleSingle(root, scanner, '_', cp, StyleNode.Type.ITALIC);
        case '~':
            scanner.next();
            cn = scanner.peek();
            if (cn == '~')
                return handleStyleDouble(root, scanner, '~', cp, StyleNode.Type.STRIKETHROUGH);
            else
                // sorry nothing
                break;
        case '[':
            if (!parseLinks)
                break;
            return handleLink(root, scanner, cp);
        default:
            break;
        }
        return false;
    }

    private boolean handleStyleDouble(Node root, Scanner scanner, char delimChar, char cp, StyleNode.Type styleType) {
        scanner.next();
        int pos = scanner.tell();
        int count = 0;
        // get count until last delimiter
        while (true) {
            int count2 = scanner.until(delimChar + "" + delimChar);
            if (count2 < 0)
                break;
            count += count2 + 1;
            scanner.next();
        }
        count--;
        if (count <= 0) {
            // failure, no terminating delimiter found
            scanner.seek(pos - 2);
            return false;
        }
        scanner.seek(pos);
        String sub = scanner.read(count);
        if (sub == null) {
            // failure, probably didn't have an end
            scanner.seek(pos - 2);
            return false;
        }
        Node child;
        if (cp == '\\') {
            root.addChild(new TextNode(delimChar + "" + delimChar));
            child = root;
        } else {
            child = new StyleNode(styleType);
            root.addChild(child);
        }
        parseInternal(child, new Scanner(sub));
        if (cp == '\\')
            root.addChild(new TextNode(delimChar + "" + delimChar));
        scanner.seek(scanner.tell() + 2);
        return true;
    }

    private boolean handleStyleSingle(Node root, Scanner scanner, char delimChar, char cp,
            @SuppressWarnings("SameParameterValue") StyleNode.Type styleType) {
        int pos = scanner.tell();
        if (delimChar == '_' && cp != Scanner.END && !Character.isWhitespace(cp) && !CharUtils.isPunctuationOrSymbol(cp)) {
            // failure, underscore delimiter requires whitespace/punctuation/symbol beforehand
            scanner.seek(pos - 1);
            return false;
        }
        if (Character.isWhitespace(scanner.peek())) {
            // failure, single delimiter requires non-whitespace after start
            scanner.seek(pos - 1);
            return false;
        }
        int count = 0;
        // get count until last delimiter
        while (true) {
            int count2 = scanner.until(delimChar);
            if (count2 < 0)
                break;
            count += count2 + 1;
            scanner.next();
            if (Character.isWhitespace(scanner.peek()))
                break;
        }
        count--;
        if (count <= 0) {
            // failure, no terminating delimiter found
            scanner.seek(pos - 1);
            return false;
        }
        scanner.seek(pos + count - 1);
        char beforeTerm = scanner.peek();
        if (Character.isWhitespace(beforeTerm)) {
            // failure, single delimiter requires non-whitespace before end
            scanner.seek(pos - 1);
            return false;
        }
        // check for mismatched delimiters
        if (beforeTerm == delimChar) {
            // failure, mismatched delimiter
            scanner.seek(pos - 1);
            return false;
        }
        scanner.seek(pos);
        String sub = scanner.read(count);
        if (sub == null) {
            // failure, probably didn't have an end
            scanner.seek(pos);
            return false;
        }
        Node child;
        if (cp == '\\') {
            root.addChild(new TextNode(delimChar + ""));
            child = root;
        } else {
            child = new StyleNode(styleType);
            root.addChild(child);
        }
        parseInternal(child, new Scanner(sub));
        if (cp == '\\')
            root.addChild(new TextNode(delimChar + ""));
        scanner.next();
        return true;
    }

    private boolean handleLink(Node root, Scanner scanner, char cp) {
        scanner.next();
        int pos = scanner.tell();
        // part 1: get text
        int textCount = scanner.until(']');
        scanner.seek(pos);
        String text = scanner.read(textCount);
        if (text == null) {
            // failure, text probably didn't have an end
            scanner.seek(pos - 1);
            return false;
        }
        // part 2: get URL
        scanner.seek(pos + textCount + 1);
        char potentialURLOpen = scanner.peek();
        if (potentialURLOpen != '(') {
            // failure, no URL start
            scanner.seek(pos - 1);
            return false;
        }
        scanner.next();
        int pos2 = scanner.tell();
        int urlCount = scanner.until(')');
        scanner.seek(pos2);
        String urlSrc = scanner.read(urlCount);
        if (urlSrc == null) {
            // failure, URL probably didn't have an end
            scanner.seek(pos - 1);
            return false;
        }
        // part 3: try to parse URL
        URL url;
        try {
            url = new URL(linkContext, urlSrc);
        } catch (MalformedURLException e) {
            // failure, URL is malformed
            scanner.seek(pos - 1);
            return false;
        }
        // part 4: put it together!
        Node child;
        if (cp == '\\') {
            root.addChild(new TextNode("["));
            child = root;
        } else {
            child = new LinkNode(url);
            root.addChild(child);
        }
        parseInternal(child, new Scanner(text));
        if (cp == '\\') {
            root.addChild(new TextNode("]("));
            parseInternal(root, new Scanner(urlSrc));
            root.addChild(new TextNode(")"));
        }
        scanner.next();
        return true;
    }

}
