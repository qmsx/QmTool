package com.quanminshangxian.tool.file;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

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
    public static boolean downloadNetworkFile(String netUrl, String descFilePath) {
        try {
            URL url = new URL(netUrl);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(descFilePath);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 切割文件
     *
     * @return 返回分片文件的文件夹路径
     */
    public static String cut(String sourcePath, int blockSize) throws IOException {
        File sourceFile = new File(sourcePath);
        int endIdx = sourcePath.lastIndexOf("/");
        String targetFolder = sourcePath.substring(0, endIdx + 1) + UUID.randomUUID().toString() + "/";
        //起始位置和实际大小
        long len = sourceFile.length();
        //切割文件数量
        int count = (int) Math.ceil(len * 1.0 / blockSize);
        //切割文件的开始位置
        long beginPos = 0;
        for (int chunk = 1; chunk <= count; chunk++) {
            beginPos = (chunk - 1) * blockSize;
            long endPos = chunk * blockSize;
            if (endPos > len) {
                endPos = len;
            }
            outToFile(sourceFile, targetFolder, chunk, beginPos, endPos);
        }
        return targetFolder;
    }

    /**
     * 输出到小文件
     *
     * @param sourceFile
     * @param chunk
     * @param beginPos
     * @param endPos
     * @throws IOException
     */
    private static void outToFile(File sourceFile, String targetFolder, int chunk, long beginPos, long endPos) throws IOException {
        String absolutePath = sourceFile.getAbsolutePath();
        int endIdx = absolutePath.lastIndexOf(File.separator);
        if (!new File(targetFolder).exists()) {
            new File(targetFolder).mkdirs();
        }
        String targetPath = targetFolder + absolutePath.substring(endIdx + 1) + "-" + chunk;
        RandomAccessFile as = new RandomAccessFile(sourceFile, "r");    //读
        RandomAccessFile os = new RandomAccessFile(targetPath, "rw");//写
        as.seek(beginPos);
        byte[] flush = new byte[10240];
        int chunkLen = (int) (endPos - beginPos);
        int len = -1;
        while ((len = as.read(flush)) != -1) {
            if (chunkLen > len) {
                os.write(flush, 0, len);
                chunkLen -= len;
            } else {
                os.write(flush, 0, chunkLen);
                break;
            }
        }
        os.close();
        as.close();
    }

    /**
     * 删除文件夹
     */
    public static void delFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            file.delete();
        }
        folder.delete();
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

    /**
     * 获取文件的md5值
     *
     * @param file
     * @return
     */
    public static String getMd5(File file) {
        try {
            return DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
