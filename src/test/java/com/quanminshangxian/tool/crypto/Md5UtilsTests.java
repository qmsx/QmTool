package com.quanminshangxian.tool.crypto;

import org.junit.Test;

public class Md5UtilsTests {

    @Test
    public void getMd5(){
        String md5 = Md5Utils.getMd5("");
        System.out.println(md5);
    }
}
