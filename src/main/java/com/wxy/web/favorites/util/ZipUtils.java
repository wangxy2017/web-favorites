package com.wxy.web.favorites.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
public class ZipUtils {

    public static void compressFile(ZipOutputStream out, File sourceFile) throws IOException {
        compress(out, sourceFile, "");
    }

    /**
     * 多层递归压缩
     *
     * @param out
     * @param sourceFile
     * @param base
     * @throws IOException
     */
    private static void compress(ZipOutputStream out, File sourceFile, String base) throws IOException {
        //如果路径为目录（文件夹）
        if (sourceFile.isDirectory()) {
            //取出文件夹中的文件（或子文件夹）
            File[] list = sourceFile.listFiles();
            //如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
            if (list == null || list.length == 0) {
                out.putNextEntry(new ZipEntry(base + File.separator));
            } else {
                //如果文件夹不为空，则递归调用compress,文件夹中的每一个文件（或文件夹）进行压缩
                for (File file : list) {
                    compress(out, file, base + File.separator + file.getName());
                }
            }
        } else {
            out.putNextEntry(new ZipEntry(base));
            Files.copy(Paths.get(sourceFile.toURI()), out);
        }
    }
}
