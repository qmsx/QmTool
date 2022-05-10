package com.quanminshangxian.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

public class ShaUtilsTests {


    @Test
    public void  ShaUtilsTes() throws NoSuchAlgorithmException {
        String aaa = ShaUtils.SHAEncrypt("aaa");
        Assert.assertEquals("7e240de74fb1ed08fa08d38063f6a6a91462a815",aaa);

    }
}
