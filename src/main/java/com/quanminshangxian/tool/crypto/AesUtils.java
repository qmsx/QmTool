package com.quanminshangxian.tool.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * aes util
 */
public class AesUtils {

    private static final String KEY_ALGORITHM = "AES";
    private static final String AES128CBC = "AES/CBC/PKCS5Padding";

    /**
     * 解密
     *
     * @param data
     * @param key
     * @param iv
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(String data, String key, String iv) throws Exception {
        return encrypt(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return byte[] 加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Key k = new SecretKeySpec(key, KEY_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES128CBC);
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, k, ivParameterSpec);
        return cipher.doFinal(data);
    }

}
