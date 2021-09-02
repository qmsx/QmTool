package com.quanminshangxian.tool.crypto;

import org.junit.Test;

public class HmacUtilsTests {

    @Test
    public void HMACEncrypt(){
        byte[] bytes = HmacUtils.HMACEncrypt("4444", "aaaa");
        System.out.println(new String(bytes));
    }
}
