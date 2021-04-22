package com.wxy.web.favorites.util;

import com.wxy.web.favorites.model.UserFile;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    /**
     * 迭代方式进行文件压缩
     *
     * @throws IOException
     */
    public static void compressFile(UserFile file, String fileName, final ZipOutputStream out) throws IOException {
        //如果是目录
        if (Integer.valueOf(1).equals(file.getIsDir())) {
            //创建文件夹
            out.putNextEntry(new ZipEntry(fileName + File.separator));
            //迭代判断，并且加入对应文件路径
            List<UserFile> children = file.getChildren();
            if (!CollectionUtils.isEmpty(children)) {
                for (UserFile child : children) {
                    compressFile(child, fileName + File.separator + child.getFilename(), out);
                }
            }
        } else {
            //创建文件
            out.putNextEntry(new ZipEntry(fileName));
            //读取文件并写出
            FileInputStream in = new FileInputStream(file.getPath());
            BufferedInputStream bis = new BufferedInputStream(in);
            byte[] bytes = new byte[1024 * 1024 * 10];
            int length;
            while ((length = bis.read(bytes)) != -1) {
                out.write(bytes, 0, length);
            }
            //关闭流
            in.close();
        }
    }
}
