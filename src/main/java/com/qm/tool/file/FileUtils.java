package com.qm.tool.file;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class FileUtils {

    /**
     * 拷贝文件
     *
     * @param sourceFilePath 源文件路径
     * @param destFilePath   拷贝后的文件路径
     * @return
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath) throws IOException {
        try {
            File src = new File(sourceFilePath);
            File desc = new File(destFilePath);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(desc));
            byte[] b = new byte[1024 * 1024];
            int len = -1;
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
                bos.flush();
            }
            bos.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 下载网络文件到本地
     *
     * @param netUrl       文件网络路径
     * @param descFilePath 保存的本地路径
     */
    public static boolean downloadFile(String netUrl, String descFilePath) throws IOException {
        URL url = new URL(netUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(descFilePath);
        int byteLen = 0;
        byte[] buffer = new byte[8192];
        while ((byteLen = is.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, byteLen);
        }
        os.close();
        is.close();
        return true;
    }

    /**
     * 文件转base64
     *
     * @param file 文件
     */
    public static String toBase64(File file) {
        try {
            Path path = Paths.get(file.getPath());
            byte[] bytes = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
