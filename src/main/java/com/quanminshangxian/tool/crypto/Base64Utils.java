package com.quanminshangxian.tool.crypto;

import java.util.Base64;

/**
 * BASE64进行加密/解密
 * <p>
 * 通常用作对二进制数据进行加密
 */
public class Base64Utils {

    /**
     * base64加密
     *
     * @param content 待加密内容
     * @return byte[]
     */
    public static byte[] encode(final String content) {
        return Base64.getEncoder().encode(content.getBytes());
    }

    /**
     * base64解密
     *
     * @param encoderContent 已加密内容
     * @return byte[]
     */
    public static byte[] decode(final byte[] encoderContent) {
        return Base64.getDecoder().decode(encoderContent);
    }

}
