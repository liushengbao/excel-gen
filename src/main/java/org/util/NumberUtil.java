package org.util;

import java.util.regex.Pattern;

/**
 * @Author: super bao
 * @Date: 2019/8/30 22:21
 */
public class NumberUtil {

    public static Pattern pattern = Pattern.compile("^(-)*([0-9]+)|(-)*([0-9]+\\.[0-9]+)$");

    public static boolean isNumber(String value) {
        return pattern.matcher(value).matches();
    }

    public static void main(String[] args) {
        System.out.println(isNumber("-1.2"));
    }

}
