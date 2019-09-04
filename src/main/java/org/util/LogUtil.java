package org.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: super bao
 * @Date: 2019/9/3 11:03
 */
public class LogUtil {

//    private static final Logger log = LoggerFactory.getLogger(LogUtil.class);

    public static void info(String content) {
//        log.info(content);
        System.out.println(content);
    }


    public static void error(String content) {
//        log.error(content);
        System.err.println(content);
    }


}
