package com.jd.jecipts.util;

/**
 * 处理字符串的一些工具方法
 *
 * @author zhongjinyan
 * @date 2018-03-07
 */
public class StringUtil {

    /**
     * 判断字符串是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return (str == null || "".equals(str) || str.length() == 0);
    }
}
