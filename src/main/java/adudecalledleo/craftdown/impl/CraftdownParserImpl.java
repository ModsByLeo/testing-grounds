package adudecalledleo.craftdown.impl;

import adudecalledleo.craftdown.node.*;
import adudecalledleo.craftdown.CraftdownParser;
import adudecalledleo.craftdown.util.NodeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static adudecalledleo.craftdown.Craftdown.LOGGER;

public final class CraftdownParserImpl implements CraftdownParser {
    private final boolean parseLinks;

    public CraftdownParserImpl(boolean parseLinks) {
        this.parseLinks = parseLinks;
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
            Node curLast = null;
            if (root.getChildCount() > 0)
                curLast = root.getChildAt(root.getChildCount() - 1);
            if (handleChar(root, scanner, c)) {
                Node textNode = new TextNode(sb.toString());
                sb.setLength(0);
                if (curLast == null)
                    root.insertChild(0, textNode);
                else
                    root.insertChild(root.getIndexOfChild(curLast), textNode);
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
        char cp, cn;
        switch (c) {
        case '*':
            // italic/bold
            cp = scanner.peekPrevious();
            scanner.next();
            cn = scanner.peek();
            if (cn == '*') {
                // bold!
                scanner.next();
                int count = scanner.until("**");
                String sub = scanner.read(count);
                if (sub == null) {
                    // failure, probably didn't have an end
                    // rewind scanner and treat as normal text instead
                    scanner.seek(scanner.tell() - (cp == '\\' ? 3 : 2));
                    break;
                }
                Node child;
                if (cp == '\\') {
                    root.addChild(new TextNode("**"));
                    child = root;
                } else
                    child = new StyleNode(StyleNode.Type.BOLD);
                root.addChild(child);
                parseInternal(child, new Scanner(sub));
                if (cp == '\\')
                    root.addChild(new TextNode("**"));
                scanner.seek(scanner.tell() + count + 2);
            } else {
                int count = scanner.until('*');
                String sub = scanner.read(count);
                if (sub == null) {
                    // failure, probably didn't have an end
                    // rewind scanner and treat as normal text instead
                    scanner.seek(scanner.tell() - (cp == '\\' ? 2 : 1));
                    break;
                }
                Node child;
                if (cp == '\\') {
                    root.addChild(new TextNode("*"));
                    child = root;
                } else
                    child = new StyleNode(StyleNode.Type.ITALIC);
                root.addChild(child);
                parseInternal(child, new Scanner(sub));
                if (cp == '\\')
                    root.addChild(new TextNode("*"));
                scanner.seek(scanner.tell() + count + 1);
            }
            return true;
        case '_':
            // italic/underline
            cp = scanner.peekPrevious();
            scanner.next();
            cn = scanner.peek();
            if (cn == '_') {
                // underline!
                scanner.next();
                int count = scanner.until("__");
                String sub = scanner.read(count);
                if (sub == null) {
                    // failure, probably didn't have an end
                    // rewind scanner and treat as normal text instead
                    scanner.seek(scanner.tell() - (cp == '\\' ? 3 : 2));
                    break;
                }
                Node child;
                if (cp == '\\') {
                    root.addChild(new TextNode("__"));
                    child = root;
                } else
                    child = new StyleNode(StyleNode.Type.UNDERLINE);
                root.addChild(child);
                parseInternal(child, new Scanner(sub));
                if (cp == '\\')
                    root.addChild(new TextNode("__"));
                scanner.seek(scanner.tell() + count + 2);
            } else {
                int count = scanner.until('_');
                String sub = scanner.read(count);
                if (sub == null) {
                    // failure, probably didn't have an end
                    // rewind scanner and treat as normal text instead
                    scanner.seek(scanner.tell() - (cp == '\\' ? 2 : 1));
                    break;
                }
                Node child;
                if (cp == '\\') {
                    root.addChild(new TextNode("_"));
                    child = root;
                } else
                    child = new StyleNode(StyleNode.Type.ITALIC);
                root.addChild(child);
                parseInternal(child, new Scanner(sub));
                if (cp == '\\')
                    root.addChild(new TextNode("_"));
                scanner.seek(scanner.tell() + count + 1);
            }
            return true;
        case '~':
            // strikethrough
            cp = scanner.peekPrevious();
            scanner.next();
            cn = scanner.peek();
            if (cn == '~') {
                // strikethrough!
                scanner.next();
                int count = scanner.until("~~");
                String sub = scanner.read(count);
                if (sub == null) {
                    // failure, probably didn't have an end
                    // rewind scanner and treat as normal text instead
                    scanner.seek(scanner.tell() - (cp == '\\' ? 3 : 2));
                    break;
                }
                Node child;
                if (cp == '\\') {
                    root.addChild(new TextNode("~~"));
                    child = root;
                } else
                    child = new StyleNode(StyleNode.Type.STRIKETHROUGH);
                root.addChild(child);
                parseInternal(child, new Scanner(sub));
                if (cp == '\\')
                    root.addChild(new TextNode("~~"));
                scanner.seek(scanner.tell() + count + 2);
            } else
                // nothing!
                break;
            return true;
        default:
            break;
        }
        return false;
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
            this.pos = pos;
            pos = Math.max(0, Math.min(pos, chars.length - 1));
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

        public char next() {
            if (pos >= chars.length)
                return END;
            return chars[pos++];
        }

        public @Nullable String read(int count) {
            if (count <= 0 || pos + count >= chars.length)
                return null;
            return new String(chars, pos, count);
        }

        public int until(char c) {
            int pos = tell();
            int count = 0;
            while (peek() != c) {
                if (peek() == END) {
                    seek(pos);
                    return -1;
                }
                count++;
                next();
            }
            seek(pos);
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
