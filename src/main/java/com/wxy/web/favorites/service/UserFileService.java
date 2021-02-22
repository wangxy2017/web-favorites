package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.UserFileRepository;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void deleteById(Integer id, Integer userId) {
        List<UserFile> allFiles = findAllFiles(id);
        long totalSize = 0L;
        List<String> pathList = new ArrayList<>();
        for (UserFile file : allFiles) {
            if (!Integer.valueOf(1).equals(file.getIsDir())) {
                totalSize += file.getSize();
                pathList.add(file.getPath());
            }
        }
        // 物理删除
        pathList.forEach(p -> {
            File file = new File(p);
            if (file.exists()) file.delete();
        });
        // 更新容量
        User user = userRepository.getOne(userId);
        user.setUsedSize(user.getUsedSize() - totalSize);
        userRepository.save(user);
        // 数据删除
        allFiles.forEach(f -> userFileRepository.deleteById(f.getId()));
    }

    public List<UserFile> findAllFiles(Integer id) {
        List<UserFile> results = new ArrayList<>();
        UserFile userFile = userFileRepository.getOne(id);
        if (Integer.valueOf(1).equals(userFile.getIsDir())) {
            for (UserFile file : userFileRepository.findByPid(userFile.getId())) {
                results.addAll(findAllFiles(file.getId()));
            }
        }
        results.add(userFile);
        return results;
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

