package com.quanminshangxian.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class RsaUtilsTests {


    @Test
    public void RsaUtils() throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        byte[] bytes2 = RSAUtils.encrypt("aaa");
        byte[] bytes = RSAUtils.decrypt(bytes2);
        Assert.assertEquals("aaa", new String(bytes));
    }

    @Test
    public void genKeyPair() throws NoSuchAlgorithmException {
        RSAUtils.genKeyPair();
    }

}
