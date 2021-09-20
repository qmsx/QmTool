package com.quanminshangxian.tool.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 */
public class MatchUtils {

    /**
     * 验证表达式和字符串是否匹配
     *
     * @param reg 正则表达式
     * @param str 字符串
     * @return
     */
    public static boolean isMatch(String reg, String str) {
        if (reg == null || str == null) {
            return false;
        }
        return Pattern.compile(reg).matcher(str).matches();
    }

    /**
     * 是否纯数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (str == null || "".equals(str.trim())) {
            return false;
        }
        String pa = "[0-9]*";
        if (str.length() > 0) {
            Pattern pattern = Pattern.compile(pa);
            Matcher isNum = pattern.matcher(str);
            if (!isNum.matches()) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 是否大写字母组合
     *
     * @param str
     * @return
     */
    public static boolean isBigCharComb(String str) {
        if (str == null || str.trim().equals("")) {
            return false;
        }
        String regex = "^[A-Z]+$";
        Matcher m = Pattern.compile(regex).matcher(str);

        // str 能够匹配regex,返回true
        return m.matches();
    }

    /**
     * 是否小写字母组合
     *
     * @param str
     * @return
     */
    public static boolean isSmartCharComb(String str) {
        if (str == null || str.trim().equals("")) {
            return false;
        }
        String regex = "^[a-z]+$";
        Matcher m = Pattern.compile(regex).matcher(str);
        // str 能够匹配regex,返回true
        return m.matches();
    }

    /**
     * 验证是否大写字母和数字的组合
     *
     * @param str
     * @return
     */
    public static boolean isBigCharAndNumComb(String str) {
        if (str == null || str.trim().equals("")) {
            return false;
        }
        String regex = "^(?![0-9]+$)(?![A-Z]+$)[0-9A-Z]{0,160}$";
        Matcher m = Pattern.compile(regex).matcher(str);
        // str 能够匹配regex,返回true
        return m.matches();
    }

    /**
     * 验证是否小写字母和数字的组合
     *
     * @param str
     * @return
     */
    public static boolean isSmartCharAndNumComb(String str) {
        if (str == null || str.trim().equals("")) {
            return false;
        }
        String regex = "^(?![0-9]+$)(?![a-z]+$)[0-9a-z]{0,160}$";
        Matcher m = Pattern.compile(regex).matcher(str);
        // str 能够匹配regex,返回true
        return m.matches();
    }

    /**
     * 验证是否字母和数字的组合
     *
     * @param str
     * @return
     */
    public static boolean isCharAndNumComb(String str) {
        if (str == null || str.trim().equals("")) {
            return false;
        }
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{0,160}$";
        Matcher m = Pattern.compile(regex).matcher(str);
        // str 能够匹配regex,返回true
        return m.matches();
    }

    /**
     * 验证是否为邮箱地址
     */
    public static boolean isEmail(String email) {
        String regex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
