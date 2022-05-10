package com.quanminshangxian.tool.crypto;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

public class Md5UtilsTests {

    @Test
    public void getMd5() throws NoSuchAlgorithmException {
        String md5 = Md5Utils.getMd5("");
        System.out.println(md5);
    }
}
