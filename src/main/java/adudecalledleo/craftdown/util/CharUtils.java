package adudecalledleo.craftdown.util;

public final class CharUtils {
    private CharUtils() { }

    public static boolean isPunctuationOrSymbol(char c) {
        final int type = Character.getType(c);
        return type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION  || type == Character.END_PUNCTUATION
                || type == Character.CONNECTOR_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION | type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.MATH_SYMBOL || type == Character.CURRENCY_SYMBOL
                || type == Character.MODIFIER_SYMBOL || type == Character.OTHER_SYMBOL;
    }
}
