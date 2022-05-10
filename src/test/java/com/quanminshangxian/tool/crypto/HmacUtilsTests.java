package com.quanminshangxian.tool.crypto;

import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacUtilsTests {

    @Test
    public void HMACEncrypt() throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] bytes = HmacUtils.HMACEncrypt("4444", "aaaa");
        System.out.println(new String(bytes));
    }
}
