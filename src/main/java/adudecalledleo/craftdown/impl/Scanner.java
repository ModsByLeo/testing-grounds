package adudecalledleo.craftdown.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Scanner {
    public static final char END = '\0';

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
        if (count <= 0 || pos + count > chars.length)
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

    @Override
    public String toString() {
        return "Scanner{" +
                "pos=" + pos +
                ",chars=\"" + new String(chars) + "\"" +
                '}';
    }
}
