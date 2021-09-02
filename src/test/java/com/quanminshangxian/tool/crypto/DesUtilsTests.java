package com.quanminshangxian.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

public class DesUtilsTests {

    @Test
    public void  DESEncrypt(){
        byte[] bytes = DesUtils.DESEncrypt("120120120","555");
        byte[] bytes1 = DesUtils.DESDecrypt("120120120", bytes);
        Assert.assertEquals("555",new String(bytes1));
    }

}
