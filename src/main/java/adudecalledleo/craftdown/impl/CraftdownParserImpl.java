package adudecalledleo.craftdown.impl;

import adudecalledleo.craftdown.node.*;
import adudecalledleo.craftdown.CraftdownParser;
import adudecalledleo.craftdown.util.NodeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

import static adudecalledleo.craftdown.Craftdown.LOGGER;

public final class CraftdownParserImpl implements CraftdownParser {
    private final boolean parseLinks;
    private final URL linkContext;

    public CraftdownParserImpl(boolean parseLinks, URL linkContext) {
        this.parseLinks = parseLinks;
        this.linkContext = linkContext;
    }

    @Override
    public @NotNull Node parse(@NotNull String src) {
        LOGGER.info("parse: STARTING!!!! src={}", src);
        Document root = new Document();
        parseInternal(root, new Scanner(src));
        NodeUtil.mergeTextNodes(root);
        LOGGER.info("parse: DONE!!!!!!");
        return root;
    }

    private void parseInternal(Node root, Scanner scanner) {
        LOGGER.info("parseInternal: root={}, scanner.chars=\"{}\"", root, new String(scanner.chars));
        final StringBuilder sb = new StringBuilder();
        char c;
        while ((c = scanner.peek()) != Scanner.END) {
            int insertPoint = 0;
            if (root.getChildCount() > 0)
                insertPoint = root.getChildCount() - 1;
            if (handleChar(root, scanner, c)) {
                Node textNode = new TextNode(sb.toString());
                sb.setLength(0);
                root.insertChild(insertPoint, textNode);
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
        LOGGER.info("parseInternal: root={} END", root);
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
            else {
                // sorry nothing
                scanner.seek(scanner.tell() - 1);
                return false;
            }
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
        int count = scanner.until(delimChar);
        // handle italic in bold/underline (***this*** or ___this___) "gracefully"
        while (scanner.peek() == delimChar) {
            count++;
            scanner.next();
        }
        scanner.seek(pos);
        String sub = scanner.read(count);
        if (sub == null) {
            // failure, probably didn't have an end
            // rewind scanner and treat as normal text instead
            scanner.seek(pos - (cp == '\\' ? 3 : 2));
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
        if (delimChar == '_' && !Character.isWhitespace(cp) && !isPunctuationOrSymbol(cp)) {
            // cancel early
            scanner.seek(pos - 1);
            return false;
        }
        int count = scanner.until(delimChar);
        scanner.seek(pos);
        String sub = scanner.read(count);
        if (sub == null) {
            // failure, probably didn't have an end
            // rewind scanner and treat as normal text instead
            scanner.seek(pos - (cp == '\\' ? 2 : 1));
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

    private static boolean isPunctuationOrSymbol(char c) {
        final int type = Character.getType(c);
        return type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION  || type == Character.END_PUNCTUATION
                || type == Character.CONNECTOR_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION | type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.MATH_SYMBOL || type == Character.CURRENCY_SYMBOL
                || type == Character.MODIFIER_SYMBOL || type == Character.OTHER_SYMBOL;
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
            // rewind scanner and treat as normal text instead
            scanner.seek(pos - (cp == '\\' ? 2 : 1));
            return false;
        }
        // part 2: get URL
        scanner.seek(pos + textCount + 1);
        char potentialURLOpen = scanner.peek();
        if (potentialURLOpen != '(') {
            // failure, no URL start
            // rewind scanner and treat as normal text instead
            scanner.seek(pos - (cp == '\\' ? 2 : 1));
            return false;
        }
        scanner.next();
        int pos2 = scanner.tell();
        int urlCount = scanner.until(')');
        scanner.seek(pos2);
        String urlSrc = scanner.read(urlCount);
        if (urlSrc == null) {
            // failure, URL probably didn't have an end
            // rewind scanner and treat as normal text instead
            scanner.seek(pos - (cp == '\\' ? 2 : 1));
            return false;
        }
        // part 3: try to parse URL
        URL url;
        try {
            url = new URL(linkContext, urlSrc);
        } catch (MalformedURLException e) {
            // failure, URL is malformed
            // rewind scanner and treat as normal text instead
            scanner.seek(pos - (cp == '\\' ? 2 : 1));
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
            root.addChild(new TextNode("("));
            parseInternal(root, new Scanner(urlSrc));
            root.addChild(new TextNode(")"));
        }
        scanner.next();
        return true;
    }

    private static final class Scanner {
        private static final char END = '\0';

        private final char[] chars;
        private int pos;

        public Scanner(@NotNull String str) {
            chars = str.toCharArray();
            pos = 0;
        }

        public int tell() {
            return pos;
        }

        public void seek(int pos) {
            this.pos = Math.max(0, Math.min(pos, chars.length - 1));
        }

        public char peek() {
            if (pos >= chars.length)
                return END;
            return chars[pos];
        }

        public char peekPrevious() {
            if (pos - 1 < 0 || pos - 1 >= chars.length)
                return END;
            return chars[pos - 1];
        }

        @SuppressWarnings("UnusedReturnValue")
        public char next() {
            if (pos >= chars.length)
                return END;
            return chars[pos++];
        }

        public @Nullable String read(int count) {
            if (count <= 0 || pos + count >= chars.length)
                return null;
            String str = new String(chars, pos, count);
            pos += count;
            return str;
        }

        public int until(char c) {
            int count = 0;
            while (peek() != c) {
                if (peek() == END) {
                    seek(pos);
                    return -1;
                }
                count++;
                next();
            }
            return count;
        }

        public int until(@NotNull String str) {
            int count = 0;
            while (peek() != END) {
                int pos = tell();
                String str2 = read(str.length());
                seek(pos);
                if (str.equals(str2))
                    return count;
                count++;
                next();
            }
            return -1;
        }
    }
}
