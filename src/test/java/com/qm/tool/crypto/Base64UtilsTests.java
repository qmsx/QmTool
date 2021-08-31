package com.qm.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

public class Base64UtilsTests {


    @Test
    public void base64Encrypt(){
        byte[] bytes = Base64Utils.encode("a");
        Assert.assertEquals("YQ==",new String(bytes));
    }

    @Test
    public void base64Decrypt(){
        String a = "YQ==";
        byte[] bytes1 = a.getBytes();
        byte[] bytes = Base64Utils.decode(bytes1);
        Assert.assertEquals("a",new String(bytes));
    }
}
