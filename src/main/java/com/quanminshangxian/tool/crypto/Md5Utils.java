package com.quanminshangxian.tool.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5加密
 * <p>
 * 是一种单向加密算法，只能加密不能解密
 */
public class Md5Utils {

    /**
     * 获取内容的md5加密字符串
     *
     * @param content
     * @return
     */
    public static String getMd5(final String content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(content.getBytes());
        byte[] b = md.digest();

        int i;

        StringBuilder buf = new StringBuilder("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        return buf.toString();
    }

}
