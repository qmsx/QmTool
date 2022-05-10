package com.quanminshangxian.tool.card;

import org.junit.Assert;
import org.junit.Test;

public class IdcardUtilsTests {

    @Test
    public void isValid() throws Exception {
        boolean valid = IdcardUtils.isValid("36070219700214402X");
        Assert.assertTrue(valid);

        boolean valid1 = IdcardUtils.isValid("555555555555555");
        Assert.assertTrue(!valid1);
    }

    @Test
    public void convert15To18() {
        String s = IdcardUtils.convert15To18("111111111111111");
        Assert.assertEquals("111111191111111113", s);
    }

    public void getBirth() {
        String birth = IdcardUtils.getBirth("360702197002144025");
        Assert.assertEquals("1970-02-14", birth);
    }

    @Test
    public void getAge() {
        int age = IdcardUtils.getAge("360702197002144025");
        Assert.assertEquals(51, age);
    }

}
