package wdd.utils.commons;


import java.util.Collection;

public class StringUtils {
    public static final String EMPTY = "";

    public static boolean nonEmpty(String str) {
        return str != null && !str.equals("") && !str.toUpperCase().equals("D41D8CD98F00B204E9800998ECF8427E");
    }

    public static boolean isEmpty(String str) {
        return !nonEmpty(str);
    }

    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    public static String join(Collection array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array.toArray(), separator, 0, array.size());
    }

    public static String join(String separator, Object... array) {
        return join(array, separator);
    }

    public static String[] split(String str, String b) {
        return str.split(b);
    }

    public static String[] split(String str, char b) {
        return str.split(String.valueOf(b));
    }

    public static String remove(String str, String b) {
        return str.replace(b, "");
    }

    public static String join(final Object[] array, String separator, final int startIndex, final int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = EMPTY;
        }

        final int noOfItems = endIndex - startIndex;
        if (noOfItems <= 0) {
            return EMPTY;
        }

        final StringBuilder buf = new StringBuilder(noOfItems * 16);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    public static boolean isNumeric(String s) {
        return s.matches("\\d+");
    }

    public static String replace(String queryString, String s, String s1) {
        return queryString.replace(s, s1);
    }
}