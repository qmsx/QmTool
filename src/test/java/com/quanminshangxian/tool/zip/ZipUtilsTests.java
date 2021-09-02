package com.quanminshangxian.tool.zip;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class ZipUtilsTests {



    @Test
    public  void zipFile() throws IOException {
        boolean b = ZipUtils.zipFile("F:\\1.jpg", "F:\\sjw\\2.zip");
        Assert.assertTrue(b);

        boolean b1 = ZipUtils.zipFile("F:\\1.jpg", "");
        Assert.assertTrue(!b1);
    }


    @Test
    public  void zipFileList() throws IOException {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("F:\\1.jpg");
        strings.add("F:\\2.jpg");
        boolean b = ZipUtils.zipFileList(strings, "F:\\sjw\\3.zip");
        Assert.assertTrue(b);

        boolean b1 = ZipUtils.zipFileList(strings, "");
        Assert.assertTrue(!b1);
    }

    @Test
    public  void zipFolder() throws IOException {
        boolean b = ZipUtils.zipFolder("F:\\test", "F:\\sjw\\4.zip");
        Assert.assertTrue(b);

        boolean b1 = ZipUtils.zipFolder("F:\\test", "");
        Assert.assertTrue(!b1);
    }
}
