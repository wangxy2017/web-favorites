package com.wxy.web.favorites.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.UserFileRepository;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import com.wxy.web.favorites.util.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class UserFileService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private UserFileRepository userFileRepository;

    @Autowired
    private UserRepository userRepository;

    public UserFile save(UserFile userFile) {
        return userFileRepository.save(userFile);
    }

    public UserFile findById(Integer id) {
        return userFileRepository.findById(id).orElse(null);
    }

    public List<UserFile> findByPid(Integer pid) {
        return userFileRepository.findByPid(pid);
    }

    public List<UserFile> findRootList(Integer userId) {
        return userFileRepository.findByUserIdAndPidIsNull(userId);
    }

    public File packageFile(Integer userId, String tempPath) throws IOException {
        // 查询用户文件
        List<UserFile> files = userFileRepository.findByUserIdAndPidIsNull(userId);
        if (!CollectionUtils.isEmpty(files)) {
            File root = new File(tempPath + File.separator + userId);
            if (root.exists()) {
                Assert.isTrue(root.delete(), "删除根目录文件失败");
            } else {
                Assert.isTrue(root.mkdirs(), "创建根目录文件失败");
            }
            // 打包
            createFile(files, root.getPath());
            return root;
        } else {
            return null;
        }
    }

    private void createFile(List<UserFile> list, String base) throws IOException {
        for (UserFile file : list) {
            if (PublicConstants.DIR_CODE.equals(file.getIsDir())) {
                // 创建文件夹
                File director = new File(base + File.separator + file.getFilename());
                if (director.exists()) {
                    Assert.isTrue(director.delete(), "删除文件夹失败");
                } else {
                    Assert.isTrue(director.mkdirs(), "创建文件夹失败");
                }
                // 查询文件夹下的文件并创建
                List<UserFile> children = userFileRepository.findByPid(file.getId());
                createFile(children, director.getPath());
            } else {
                File disk = new File(file.getPath());
                if (disk.exists()) {
                    File out = new File(base + File.separator + file.getFilename());
                    if (out.exists()) {
                        Assert.isTrue(out.delete(), "删除文件失败");
                    } else {
                        Assert.isTrue(out.createNewFile(), "创建文件失败");
                    }
                    FileCopyUtils.copy(disk, out);
                }
            }
        }
    }

    public void deleteById(Integer id, Integer userId) throws IOException {
        UserFile userFile = userFileRepository.findById(id).orElse(null);
        if (userFile != null) {
            List<UserFile> deletingFiles = new ArrayList<>();
            addDeletingFile(deletingFiles, userFile);
            long totalSize = 0L;
            List<String> pathList = new ArrayList<>();
            for (UserFile file : deletingFiles) {
                if (!PublicConstants.DIR_CODE.equals(file.getIsDir())) {
                    totalSize += file.getSize();
                    pathList.add(file.getPath());
                }
            }
            // 物理删除
            for (String path : pathList) {
                Files.delete(Paths.get(path));
            }
            // 更新容量
            User user = userRepository.getOne(userId);
            long size = user.getUsedSize() - totalSize;
            user.setUsedSize(size < 0 ? 0 : size);// 容量计算误差修正
            userRepository.save(user);
            // 数据删除
            userFileRepository.deleteAll(deletingFiles);
        }
    }

    private void addDeletingFile(List<UserFile> deletingFiles, UserFile userFile) {
        if (PublicConstants.DIR_CODE.equals(userFile.getIsDir())) {
            List<UserFile> children = userFileRepository.findByPid(userFile.getId());
            for (UserFile child : children) {
                addDeletingFile(deletingFiles, child);
            }
        }
        deletingFiles.add(userFile);
    }

    public String saveFile(InputStream input) throws IOException {
        Path folder = Paths.get(appConfig.getFileRepository(), StringUtils.join(RandomUtil.randomString(appConfig.getFileDeepLevel()).toCharArray(), File.separatorChar));
        if (Files.notExists(folder)) {
            Files.createDirectories(folder);
        }
        Path file = Paths.get(folder.toString(), UUID.randomUUID().toString().replaceAll("-", ""));
        Files.copy(input, file);
        return file.toString();
    }

    public PageInfo<UserFile> findPageList(Integer userId, String name, Integer pid, Integer pageNum, Integer pageSize) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "filename"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        Page<UserFile> page;
        if (StringUtils.isNotBlank(name) && pid == null) {
            page = userFileRepository.findByUserIdAndFilenameLike(userId, "%" + name + "%", pageable);
        } else {
            page = userFileRepository.findByUserIdAndPid(userId, pid, pageable);
        }
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    public List<UserFile> findFloorsByPid(Integer pid) {
        List<UserFile> floors = new ArrayList<>();
        while (pid != null) {
            UserFile userFile = userFileRepository.findById(pid).orElse(null);
            if (userFile != null) {
                floors.add(userFile);
                pid = userFile.getPid();
            }
        }
        Collections.reverse(floors);
        return floors;
    }

    public UserFile findByShareId(String shareId) {
        return userFileRepository.findByShareId(shareId);
    }
}

