package com.qm.tool.file;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FileUtilsTests {

    @Test
    public void copyFile() throws IOException {
        /*boolean b = FileUtils.copyFile("F:\\sjw\\idea\\aaa.zip", "F:\\sjw\\3.zip");
        Assert.assertTrue(b);*/ //55947
       /* boolean b2 = FileUtils.copyFile2("F:\\sjw\\idea\\aaa.zip", "F:\\sjw\\3.zip");
        Assert.assertTrue(b2);*///61536
        boolean b3 = FileUtils.copyFile("F:\\sjw\\idea\\aaa.zip", "");
        Assert.assertTrue(!b3);// 53606毫秒

        //239672毫秒

        boolean b12 = FileUtils.copyFile("F:\\1.jpg", "F:\\sjw\\5.jpg");
        Assert.assertTrue(b12);
    }
    @Test
    public void downloadNetworkFile() throws IOException {
        boolean b = FileUtils.downloadFile("https://img.alicdn.com/imgextra/i3/326997967/TB2vpmgrr1YBuNjSszhXXcUsFXa_!!326997967.jpg", "F:\\sjw\\10.jpg");
        Assert.assertTrue(b);

    }

    @Test
    public void toBase64(){
        File file = new File("F:\\sjw\\test\\12.txt");
        String s = FileUtils.toBase64(file);
        System.out.println(s);
    }
}

