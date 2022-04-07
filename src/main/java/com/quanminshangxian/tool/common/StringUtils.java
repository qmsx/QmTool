package com.quanminshangxian.tool.common;

/**
 * 字符串工具类
 */
public class StringUtils {

    /**
     * 判断字符串是否相等
     *
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equals(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        if (str == null || "".equals(str.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 截取字符串部分内容
     *
     * @param content
     * @param interceptLen
     * @return
     */
    public static String interceptStr(String content, int interceptLen) {
        if (content == null) {
            return null;
        }
        if (content.length() <= interceptLen) {
            return content;
        }
        String interceptStr = content.substring(0, interceptLen);
        if (content.contains(interceptStr)) {
            return interceptStr;
        }
        interceptStr = content.substring(0, interceptLen + 1);
        return interceptStr;
    }

}
