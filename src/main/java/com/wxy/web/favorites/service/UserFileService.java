package com.wxy.web.favorites.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.dao.UserFileRepository;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import com.wxy.web.favorites.util.JpaUtils;
import com.wxy.web.favorites.util.SqlUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

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
        if (userFile.getId() != null) {
            userFileRepository.findById(userFile.getId()).ifPresent(source -> JpaUtils.copyNotNullProperties(source, userFile));
        }
        return userFileRepository.save(userFile);
    }

    public UserFile findById(Integer id) {
        return userFileRepository.findById(id).orElse(null);
    }

    public List<UserFile> findByPid(Integer pid) {
        return userFileRepository.findByPid(pid);
    }

    public List<UserFile> findRootList(Integer userId) {
        return userFileRepository.findByUserIdAndPid(userId, 0);
    }

    /**
     * 将用户所有文件，按照层级结构，打包到临时目录
     *
     * @param userId
     * @param tempPath
     * @return
     * @throws IOException
     */
    public Path packageFile(Integer userId, String tempPath) throws IOException {
        // 查询用户文件
        List<UserFile> rootList = userFileRepository.findByUserIdAndPid(userId, 0);
        if (!CollectionUtils.isEmpty(rootList)) {
            Path root = Paths.get(tempPath, String.valueOf(userId));
            // 打包前，删除历史打包
            cleanHistory(root);
            createFile(rootList, root);
            return root;
        } else {
            return null;
        }
    }

    public void cleanHistory(Path base) throws IOException {
        if (Files.exists(base)) {
            Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void createFile(List<UserFile> list, Path base) throws IOException {
        if (Files.notExists(base)) {
            Files.createDirectories(base);
        }
        for (UserFile file : list) {
            if (PublicConstants.DIR_CODE.equals(file.getIsDir())) {// 如果是文件夹，则创建文件夹
                Path director = Paths.get(base.toString(), file.getFilename());
                Files.createDirectories(director);
                // 查询文件夹下的文件并创建
                List<UserFile> children = userFileRepository.findByPid(file.getId());
                createFile(children, director);
            } else {// 如果是文件，则拷贝文件到临时目录
                Path source = Paths.get(file.getPath());
                if (Files.exists(source)) {
                    Path target = Paths.get(base.toString(), file.getFilename());
                    Files.copy(source, target);
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
            // 物理删除文件(目录不删)
            for (String path : pathList) {
                Files.deleteIfExists(Paths.get(path));
            }
            // 更新容量
            User user = userRepository.getOne(userId);
            long size = Optional.ofNullable(user.getUsedSize()).orElse(0L) - totalSize;
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
        Path folder = Paths.get(appConfig.getFileRepository(), Strings.join(Collections.singletonList(RandomUtil.randomString(appConfig.getFileDeepLevel()).toCharArray()), File.separatorChar));
        if (Files.notExists(folder)) {
            Files.createDirectories(folder);
        }
        Path file = Paths.get(folder.toString(), UUID.randomUUID().toString().replaceAll("-", ""));
        Files.copy(input, file);
        return file.toString();
    }

    public PageInfo<UserFile> findPageList(Integer userId, String name, Integer pid, Integer pageNum, Integer pageSize) {
        name = SqlUtils.trimAndEscape(name);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "filename"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        Page<UserFile> page;
        if (StrUtil.isNotBlank(name) && pid == null) {
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

    public UserFile findByPidAndFilename(Integer pid, String filename) {
        return userFileRepository.findByPidAndFilename(pid, filename);
    }

    public List<Integer> findAllChildDir(UserFile file) {
        List<Integer> list = new ArrayList<>();
        buildList(file, list);
        return list;
    }

    private void buildList(UserFile file, List<Integer> list) {
        if (file != null && PublicConstants.DIR_CODE.equals(file.getIsDir())) {
            list.add(file.getId());
            userFileRepository.findByPid(file.getId()).forEach(c -> buildList(c, list));
        }
    }
}

