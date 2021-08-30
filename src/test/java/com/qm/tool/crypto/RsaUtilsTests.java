package com.qm.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

public class RsaUtilsTests {


    @Test
    public void RsaUtils() {
        byte[] bytes2 = RSAUtils.encrypt("aaa");
        byte[] bytes = RSAUtils.decrypt(bytes2);
        Assert.assertEquals("aaa", new String(bytes));
    }

    @Test
    public void genKeyPair() throws NoSuchAlgorithmException {
        RSAUtils.genKeyPair();
    }

}
