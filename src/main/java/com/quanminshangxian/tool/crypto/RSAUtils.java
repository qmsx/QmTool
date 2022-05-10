package com.quanminshangxian.tool.crypto;

import com.quanminshangxian.tool.form.QmFormClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * RSA非对称加密/解密
 * <p>
 * 非对称加密算法的典型代表，既能加密、又能解密。和对称加密算法比如DES的明显区别在于用于加密、解密的密钥是不同的。使用RSA算法，只要密钥足够长(一般要求1024bit)，加密的信息是不能被破解的。
 */
public class RSAUtils {

    private static final String ALGORITHM_RSA = "rsa";
    private static final String ALGORITHM_MD5_RSA = "MD5withRSA";
    private static KeyPair keyPair;
    private static SecureRandom secureRandom;

    static {
        secureRandom = new SecureRandom();
        try {
            //创建密钥对KeyPair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
            //密钥长度推荐为1024位
            keyPairGenerator.initialize(1024);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("rsa init error");
        }
    }

    /**
     * 获取私钥，用于RSA非对称加密.
     *
     * @return PrivateKey
     */
    public static PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    /**
     * 获取公钥，用于RSA非对称加密.
     *
     * @return PublicKey
     */
    public static PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    /**
     * 获取数字签名，用于RSA非对称加密.
     *
     * @return byte[]
     */
    public static byte[] getSignature(final byte[] encoderContent) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(ALGORITHM_MD5_RSA);
        signature.initSign(keyPair.getPrivate());
        signature.update(encoderContent);
        return signature.sign();
    }

    /**
     * 验证数字签名，用于RSA非对称加密.
     *
     * @return byte[]
     */
    public static boolean verifySignature(final byte[] encoderContent, final byte[] signContent) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(ALGORITHM_MD5_RSA);
        signature.initVerify(keyPair.getPublic());
        signature.update(encoderContent);
        return signature.verify(signContent);
    }

    /**
     * RSA加密
     *
     * @param content 待加密内容
     * @return byte[]
     */
    public static byte[] encrypt(final String content) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return processCipher(content.getBytes(), keyPair.getPrivate(), Cipher.ENCRYPT_MODE, ALGORITHM_RSA);
    }

    /**
     * RSA解密
     *
     * @param encoderContent 已加密内容
     * @return byte[]
     */
    public static byte[] decrypt(final byte[] encoderContent) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        return processCipher(encoderContent, keyPair.getPublic(), Cipher.DECRYPT_MODE, ALGORITHM_RSA);
    }

    /**
     * 加密/解密处理
     *
     * @param processData 待处理的数据
     * @param key         提供的密钥
     * @param opsMode     工作模式
     * @param algorithm   使用的算法
     * @return byte[]
     */
    private static byte[] processCipher(final byte[] processData, final Key key,
                                        final int opsMode, final String algorithm)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(algorithm);
        //初始化
        cipher.init(opsMode, key, secureRandom);
        return cipher.doFinal(processData);
    }

    /**
     * 随机生成密钥对
     *
     * @throws NoSuchAlgorithmException
     */
    public static String[] genKeyPair() {
        RSAPrivateKey privateKey = (RSAPrivateKey) getPrivateKey();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) getPublicKey();  // 得到公钥
        String publicKeyString = new String(Base64Utils.encode(new String(publicKey.getEncoded())));
        // 得到私钥字符串
        String privateKeyString = new String(Base64Utils.encode(new String(privateKey.getEncoded())));
        // 将公钥和私钥保存到Map
        return new String[]{
                publicKeyString,
                privateKeyString
        };
    }

}
