package wdd.utils.mongo;

import java.util.Stack;

public class ConvertRadixUtil {

    /**
     * 用"0"补足一个字符串到指定长度
     *
     * @param str  -  源字符串
     * @param size - 补足后应达到的长度
     * @return - 补零后的结果
     */
    public static String fillZero(String str, int size) {
        String result;
        if (str.length() < size) {
            char[] s = new char[size - str.length()];
            for (int i = 0; i < (size - str.length()); i++) {
                s[i] = '0';
            }
            result = new String(s) + str;
        } else {
            result = str;
        }
        return result;
    }


    public static void main(String[] args) {
        String aa = "7e6fbe3";
        System.out.println(larget12String62to10("0GgTNH7H2aFG"));
        System.out.println(aa);
        System.out.println(larget19String10to62("6570878141525397504"));
    }

    public static final char[] array = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'g',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z'};

    /**
     * 大数据量32位16进制数转化为62进制,23位
     *
     * @param value
     * @return
     */
    public static String larget32String16to62(String value) {
        String value1 = value.substring(0, 10);
        String value2 = value.substring(10, 21);
        String value3 = value.substring(21);
        return convert16to62(value1) + fillZero(convert16to62(value2), 8) + fillZero(convert16to62(value3), 8);
    }

    /**
     * 大数据量18~19位10进制数转化为62进制,12位
     *
     * @param value
     * @return
     */
    public static String larget19String10to62(String value) {
        String value1 = value.substring(0, 9);
        String value2 = value.substring(9);
        return convert10to62(value1) + fillZero(convert10to62(value2), 6);
    }

    /**
     * 大数据量转化的62进制转化为18~19位10进制数
     */
    public static String larget12String62to10(String value) {
        String value1 = value.substring(value.length() - 6);
        String value2 = value.substring(0, value.length() - 6);
        return convert62to10(value2) + "" + convert62to10(value1);
    }


    /**
     * 转化16进制数为62进制数
     *
     * @param value
     * @return
     */
    public static String convert16to62(String value) {
        value = Long.valueOf(value, 16).toString();
        return convert10to62(value);
    }

    /**
     * 转化62进制数为16进制数
     *
     * @param value
     * @return
     */
    public static String convert62to16(String value) {
        Long number = convert62to10(value);
        return Long.toHexString(number);
    }


    /**
     * 转化10进制数为62进制数
     *
     * @param value
     * @return
     */
    public static String convert10to62(String value) {
        Long rest = Long.parseLong(value);
        Stack<Character> stack = new Stack<Character>();
        StringBuilder result = new StringBuilder(0);
        while (rest != 0) {
            stack.add(array[new Long((rest - (rest / 62) * 62)).intValue()]);
            rest = rest / 62;
        }
        for (; !stack.isEmpty(); ) {
            result.append(stack.pop());
        }
        return result.toString();
    }

    /**
     * 转化62进制数为10进制数
     *
     * @param value
     * @return
     */
    public static long convert62to10(String value) {
        long multiple = 1L;
        long result = 0L;
        Character c;
        for (int i = 0; i < value.length(); i++) {
            c = value.charAt(value.length() - i - 1);
            result += c62value(c) * multiple;
            multiple = multiple * 62;
        }
        return result;
    }

    private static int c62value(Character c) {
        for (int i = 0; i < array.length; i++) {
            if (c == array[i]) {
                return i;
            }
        }
        return -1;
    }

}
