package com.quanminshangxian.tool.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA(Secure Hash Algorithm，安全散列算法)
 * <p>
 * 数字签名等密码学应用中重要的工具，被广泛地应用于电子商务等信息安全领域
 */
public class ShaUtils {

    private static final String ALGORITHM_SHA = "sha";

    /**
     * SHA加密
     *
     * @param content 待加密内容
     * @return String
     */
    public static String SHAEncrypt(final String content) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance(ALGORITHM_SHA);
        byte[] sha_byte = sha.digest(content.getBytes());
        StringBuilder hexValue = new StringBuilder();
        for (byte b : sha_byte) {
            //将其中的每个字节转成十六进制字符串：byte类型的数据最高位是符号位，通过和0xff进行与操作，转换为int类型的正整数。
            String toHexString = Integer.toHexString(b & 0xff);
            hexValue.append(toHexString.length() == 1 ? "0" + toHexString : toHexString);
        }
        return hexValue.toString();
    }

}
