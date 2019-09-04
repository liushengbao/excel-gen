package org.util;

/**
 * @Author: super bao
 * @Date: 2019/9/2 11:50
 */
public class StringUtil {

    public static boolean isEmpty(String val) {
        return val == null || val.equals("") || val.length() <= 0;
    }

    public static int compare(final String str1, final String str2) {
        if (str1 == str2) {
            return 0;
        }
        if (str1.equals("id")) {
            return -1;
        }
        if (str2.equals("id")) {
            return -1;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return -1;
        }
        return str1.compareTo(str2);
    }

}
