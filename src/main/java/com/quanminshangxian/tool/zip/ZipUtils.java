package com.quanminshangxian.tool.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 压缩文件
 */
public class ZipUtils {

    /**
     * 压缩文件（单个）
     *
     * @param sourceFile       需要压缩的文件路径
     * @param compressFilePath 压缩后的文件路径
     * @return
     */
    public static boolean zipFile(String sourceFile, String compressFilePath) throws IOException {
        //输出压缩包
        FileOutputStream fos = new FileOutputStream(compressFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        //被压缩文件
        File fileToZip = new File(sourceFile);
        FileInputStream fis = new FileInputStream(fileToZip);
        //向压缩包中添加文件
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        fis.close();
        fos.close();
        return true;
    }

    /**
     * 压缩文件（多个）
     *
     * @param sourceFileList   需要压缩的文件路径列表
     * @param compressFilePath 压缩后的文件路径
     * @return
     */
    public static boolean zipFileList(List<String> sourceFileList, String compressFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(compressFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        //向压缩包中添加多个文件
        for (String srcFile : sourceFileList) {
            File fileToZip = new File(srcFile);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        fos.close();
        return true;
    }

    /**
     * 压缩文件夹
     *
     * @param folderPath       需要压缩的文件夹路径
     * @param compressFilePath 压缩后的文件路径
     * @return
     */
    public static boolean zipFolder(String folderPath, String compressFilePath) throws IOException {
        //压缩结果输出，即压缩包
        FileOutputStream fos = new FileOutputStream(compressFilePath);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(folderPath);
        //递归压缩文件夹
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        //关闭输出流
        zipOut.close();
        fos.close();
        return true;
    }

    /**
     * 将fileToZip文件夹及其子目录文件递归压缩到zip文件中
     *
     * @param fileToZip 递归当前处理对象，可能是文件夹，也可能是文件
     * @param fileName  fileToZip文件或文件夹名称
     * @param zipOut    压缩文件输出流
     * @throws IOException
     */
    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        //不压缩隐藏文件夹
        if (fileToZip.isHidden()) {
            return;
        }
        //判断压缩对象如果是一个文件夹
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                //如果文件夹是以“/”结尾，将文件夹作为压缩箱放入zipOut压缩输出流
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                //如果文件夹不是以“/”结尾，将文件夹结尾加上“/”之后作为压缩箱放入zipOut压缩输出流
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            //遍历文件夹子目录，进行递归的zipFile
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            //如果当前递归对象是文件夹，加入ZipEntry之后就返回
            return;
        }
        //如果当前的fileToZip不是一个文件夹，是一个文件，将其以字节码形式压缩到压缩包里面
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    /**
     * 解压缩文件
     */
    public static void unzip(String sourceFile, String unzipFolderPath) throws Exception {
        //解压的目标目录
        File destDir = new File(unzipFolderPath);

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceFile));
        //获取压缩包中的entry，并将其解压
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            //解压完成一个entry，再解压下一个
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    //在解压目标文件夹，新建一个文件
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("该解压项在目标文件夹之外: " + zipEntry.getName());
        }
        return destFile;
    }

}
