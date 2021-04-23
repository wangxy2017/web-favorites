package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.UserFileRepository;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class UserFileService {

    @Value("${app.file-repository}")
    private String repository;

    @Autowired
    private UserFileRepository userFileRepository;

    @Autowired
    private UserRepository userRepository;

    public UserFile save(UserFile userFile) {
        return userFileRepository.save(userFile);
    }

    public UserFile findById(Integer id) {
        return userFileRepository.getOne(id);
    }

    public List<UserFile> findByPid(Integer pid) {
        return userFileRepository.findByPid(pid);
    }

    public List<UserFile> findRootList(Integer userId) {
        return userFileRepository.findByUserIdAndPidIsNull(userId);
    }

    public List<UserFile> searchFiles(Integer userId, String filename) {
        return userFileRepository.findByUserIdAndFilenameLike(userId, filename);
    }

    public File packageFileByUserId(Integer userId, String tempPath) throws IOException {
        // 查询用户文件
        List<UserFile> files = userFileRepository.findByUserIdAndPidIsNull(userId);
        File root = new File(tempPath + File.separator + userId);
        if (root.exists()) {
            root.delete();
        }
        root.mkdirs();
        // 打包
        createFile(files, root.getName());
        return root;
    }

    public void createFile(List<UserFile> list, String path) throws IOException {
        for (UserFile file : list) {
            if (Integer.valueOf(1).equals(file.getIsDir())) {
                List<UserFile> children = userFileRepository.findByPid(file.getId());
                createFile(children, file.getFilename());
            } else {
                File out = new File(path + File.separator + file.getFilename());
                if (out.exists()) {
                    File in = new File(file.getPath());
                    in.createNewFile();
                    FileCopyUtils.copy(in, out);
                }
            }
        }
    }

    public void deleteById(Integer id, Integer userId) {
        UserFile userFile = userFileRepository.getOne(id);
        if (userFile.getId() != null) {
            List<UserFile> deletingFiles = new ArrayList<>();
            addDeletingFile(deletingFiles, userFile);

            long totalSize = 0L;
            List<String> pathList = new ArrayList<>();
            for (UserFile file : deletingFiles) {
                if (!Integer.valueOf(1).equals(file.getIsDir())) {
                    totalSize += file.getSize();
                    pathList.add(file.getPath());
                }
            }
            // 物理删除
            pathList.forEach(p -> new File(p).delete());
            // 更新容量
            User user = userRepository.getOne(userId);
            user.setUsedSize(user.getUsedSize() - totalSize);
            userRepository.save(user);
            // 数据删除
            userFileRepository.deleteAll(deletingFiles);
        }
    }

    private void addDeletingFile(List<UserFile> deletingFiles, UserFile userFile) {
        if (Integer.valueOf(1).equals(userFile.getIsDir())) {
            List<UserFile> children = userFileRepository.findByPid(userFile.getId());
            for (UserFile child : children) {
                addDeletingFile(deletingFiles, child);
            }
        }
        deletingFiles.add(userFile);
    }

    public String writeFile(InputStream input) throws IOException {
        String sequence = "1234567890qwertyuiopasdfghjklzxcvbnm";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            char c = sequence.charAt(new Random().nextInt(sequence.length()));
            sb.append(c).append(File.separator);
        }
        File folder = new File(repository + File.separator + sb);
        if (!folder.exists()) {
            boolean bool = folder.mkdirs();
            if (bool) {
                File file = new File(folder.getPath() + File.separator + UUID.randomUUID().toString().replaceAll("-", ""));
                BufferedInputStream bin = new BufferedInputStream(input);
                FileOutputStream out = new FileOutputStream(file);
                byte[] buffer = new byte[1024 * 1024 * 10];
                int i = bin.read(buffer);
                while (i != -1) {
                    out.write(buffer, 0, i);
                    i = bin.read(buffer);
                }
                return file.getPath();
            }
        }
        throw new IOException();
    }
}

