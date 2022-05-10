package com.quanminshangxian.tool.crypto;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class DesUtilsTests {

    @Test
    public void  DESEncrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        byte[] bytes = DesUtils.DESEncrypt("120120120","555");
        byte[] bytes1 = DesUtils.DESDecrypt("120120120", bytes);
        Assert.assertEquals("555",new String(bytes1));
    }

}
