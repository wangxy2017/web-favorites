package com.wxy.web.favorites.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.UserFileService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.ZipUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/file")
@Slf4j
@Api(tags = "文件")
@Secured("file")
public class FileController {

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppConfig appConfig;

    @GetMapping("/count")
    @ApiOperation(value = "查询文件总数")
    public ApiResponse count() {
        SecurityUser user = ContextUtils.getCurrentUser();
        List<UserFile> list = userFileService.findRootList(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("count", list.size());
        return ApiResponse.success(data);
    }

    @GetMapping("/exists/{id}")
    @ApiOperation(value = "判断文件是否存在")
    public ApiResponse exists(@PathVariable Integer id) {
        UserFile file = userFileService.findById(id);
        if (file != null && StrUtil.isNotBlank(file.getPath()) && Files.exists(Paths.get(file.getPath()))) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/share/{id}")
    @ApiOperation(value = "分享文件")
    public ApiResponse share(@PathVariable Integer id) {
        UserFile file = userFileService.findById(id);
        if (file != null && StrUtil.isNotBlank(file.getPath()) && Files.exists(Paths.get(file.getPath()))) {
            if (StrUtil.isBlank(file.getShareId())) {
                file.setShareId(UUID.randomUUID().toString().replaceAll("-", ""));
                userFileService.save(file);
            }
            return ApiResponse.success(file.getShareId());
        }
        return ApiResponse.error();
    }

    /**
     * 获取文件列表
     *
     * @param name 文件名称
     * @param pid  父文件夹
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "获取文件列表")
    public ApiResponse list(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer pid,
                            @RequestParam(required = false) Integer pageNum,
                            @RequestParam(required = false) Integer pageSize) {
        SecurityUser user = ContextUtils.getCurrentUser();
        PageInfo<UserFile> page = userFileService.findPageList(user.getId(), name, pid, pageNum, pageSize);
        List<UserFile> floors = userFileService.findFloorsByPid(pid);
        HashMap<String, Object> data = new HashMap<>();
        data.put("parent", pid);
        data.put("page", page);
        data.put("floors", floors);
        return ApiResponse.success(data);
    }

    @PostMapping("/rename")
    @ApiOperation(value = "重命名")
    public ApiResponse rename(@RequestParam Integer id, @RequestParam String filename) {
        UserFile file = userFileService.findById(id);
        if (file != null && StrUtil.isNotBlank(filename)) {
            file.setFilename(filename);
            file.setUpdateTime(new Date());
            userFileService.save(file);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/share/download/{shareId}")
    @ApiOperation(value = "下载分享文件")
    public void shareDownload(HttpServletResponse response, @PathVariable String shareId) throws IOException {
        UserFile userFile = userFileService.findByShareId(shareId);
        Assert.notNull(userFile, ErrorConstants.RESOURCE_NOT_FOUND_MSG);
        download(response, userFile.getId());
    }

    @GetMapping("/share/cancel/{id}")
    @ApiOperation(value = "取消分享")
    public ApiResponse shareCancel(HttpServletResponse response, @PathVariable Integer id) {
        userService.shareCancel(id);
        return ApiResponse.success();
    }

    @GetMapping("/download/{id}")
    @ApiOperation(value = "下载文件")
    public void download(HttpServletResponse response, @PathVariable Integer id) throws IOException {
        UserFile userFile = userFileService.findById(id);
        Assert.notNull(userFile, ErrorConstants.RESOURCE_NOT_FOUND_MSG);
        Assert.isTrue(!PublicConstants.DIR_CODE.equals(userFile.getIsDir()) && StrUtil.isNotBlank(userFile.getPath()), "数据异常");
        Path file = Paths.get(userFile.getPath());
        Assert.isTrue(Files.exists(file), ErrorConstants.FILE_IS_DELETED_MSG);
        response.setContentType(ContentType.OCTET_STREAM.getValue());
        response.addHeader("Content-Disposition", "attachment;fileName=" + new String(userFile.getFilename().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        try (ServletOutputStream out = response.getOutputStream()) {
            Files.copy(file, out);
        }
    }

    @GetMapping("/downloadAll")
    @ApiOperation(value = "备份")
    public void downloadAll(HttpServletResponse response) throws IOException {
        SecurityUser user = ContextUtils.getCurrentUser();
        String tempPath = ContextUtils.getRequest().getServletContext().getRealPath("/");
        Path packageFile = userFileService.packageFile(user.getId(), tempPath);
        Assert.notNull(packageFile, ErrorConstants.RESOURCE_NOT_FOUND_MSG);
        try (ZipOutputStream out = new ZipOutputStream(response.getOutputStream())) {
            out.setMethod(ZipEntry.DEFLATED);
            out.setLevel(appConfig.getFileCompressLevel());
            // 压缩文件
            ZipUtils.compressFile(out, packageFile.toFile());
        }
        // 删除临时文件
        userFileService.cleanHistory(packageFile);
    }

    private String getNoRepeatFilename(Integer pid, String filename) {
        String prefix = FileUtil.getPrefix(filename);
        String suffix = FileUtil.getSuffix(filename);
        String name = filename;
        int index = 0;
        while (userFileService.findByPidAndFilename(pid, name) != null) {
            name = prefix + "_" + ++index + (StrUtil.isNotBlank(suffix) ? "." + suffix : "");
        }
        return name;
    }

    @PostMapping("/upload")
    @ApiOperation(value = "批量上传")
    public ApiResponse upload(@RequestParam("file") MultipartFile[] files, @RequestParam(required = false) Integer pid) throws IOException {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
        if (Optional.ofNullable(user.getCapacity()).orElse(0L) > 0) {
            long restSize = Optional.ofNullable(user.getCapacity()).orElse(0L) - Optional.ofNullable(user.getUsedSize()).orElse(0L);
            long totalSize = 0;
            for (MultipartFile file : files) {
                totalSize += file.getSize();
            }
            long freeSize = Arrays.stream(File.listRoots()).mapToLong(File::getFreeSpace).sum();
            Date now = new Date();
            if (restSize > totalSize && freeSize * PublicConstants.DISK_LIMIT_RATE > totalSize) {
                for (MultipartFile file : files) {
                    String path = userFileService.saveFile(file.getInputStream());
                    String filename = Objects.requireNonNull(file.getOriginalFilename()).replaceAll(" ", "+");
                    UserFile userFile = new UserFile().setUserId(user.getId()).setPid(pid).setCreateTime(now).setUpdateTime(now).setFilename(getNoRepeatFilename(pid, filename)).setPath(path).setSize(file.getSize());
                    userFileService.save(userFile);
                    long newSize = Optional.ofNullable(user.getUsedSize()).orElse(0L) + file.getSize();
                    long capacity = Optional.ofNullable(user.getCapacity()).orElse(0L);
                    user.setUsedSize(Math.min(newSize, capacity));// 容量误差修正
                    userService.save(user);
                }
                return ApiResponse.success();
            }
        }
        return ApiResponse.error(ErrorConstants.NO_SPACE_LEFT_MSG);
    }

    @GetMapping("/back")
    @ApiOperation(value = "返回上一级")
    public ApiResponse goBack(@RequestParam(required = false) Integer pid) {
        if (pid == null) {
            return ApiResponse.success();
        } else {
            return ApiResponse.success(Optional.ofNullable(userFileService.findById(pid)).map(UserFile::getPid).orElse(null));
        }
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除文件")
    public ApiResponse delete(@RequestParam Integer id) throws IOException {
        SecurityUser user = ContextUtils.getCurrentUser();
        userFileService.deleteById(id, user.getId());
        return ApiResponse.success();
    }

    @PostMapping("/deleteMore")
    @ApiOperation(value = "批量删除")
    public ApiResponse deleteMore(@RequestParam String ids) throws IOException {
        SecurityUser user = ContextUtils.getCurrentUser();
        String[] split = ids.split(PublicConstants.ID_DELIMITER);
        for (String s : split) {
            userFileService.deleteById(Integer.valueOf(s), user.getId());
        }
        return ApiResponse.success();
    }

    @PostMapping("/folder")
    @ApiOperation(value = "新建文件夹")
    public ApiResponse newFolder(@RequestParam String filename, @RequestParam(required = false) Integer pid) {
        UserFile file = userFileService.findByPidAndFilename(pid, filename);
        Assert.isNull(file, "文件夹已存在");
        SecurityUser user = ContextUtils.getCurrentUser();
        Date now = new Date();
        UserFile file1 = new UserFile().setUserId(user.getId()).setPid(pid).setCreateTime(now).setUpdateTime(now).setFilename(filename).setIsDir(1).setSize(0L);
        userFileService.save(file1);
        return ApiResponse.success();
    }

    /**
     * 移动文件
     *
     * @return
     */
    @PostMapping("/move")
    @ApiOperation(value = "移动文件")
    public ApiResponse move(@RequestParam String ids, @RequestParam(required = false) Integer pid) {
        for (String id : ids.split(PublicConstants.ID_DELIMITER)) {
            UserFile file = userFileService.findById(Integer.valueOf(id));
            // 不能移动到自己或其子文件夹
            List<Integer> list = userFileService.findAllChildDir(file);
            if (!list.contains(pid)) {
                file.setPid(pid);
                userFileService.save(file);
            }
        }
        return ApiResponse.success();
    }


    @GetMapping("/view")
    @ApiOperation(value = "预览")
    public ApiResponse view(@RequestParam Integer id) {
        UserFile file = userFileService.findById(id);
        if (file != null) {
            String suffix = FileUtil.getSuffix(file.getFilename());
            if (StrUtil.isNotBlank(suffix) && Optional.ofNullable(appConfig.getFileSuffixes()).orElse("").contains(suffix)) {
                StringBuilder sb = new StringBuilder();
                try (FileChannel channel = new RandomAccessFile(file.getPath(), "r").getChannel()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while (channel.read(buffer) != -1) {
                        buffer.flip();
                        sb.append(StandardCharsets.UTF_8.decode(buffer));
                        buffer.clear();
                    }
                } catch (IOException ignored) {
                }
                return ApiResponse.success(sb.toString());
            }
        }
        return ApiResponse.error();
    }

    @GetMapping("/tree")
    @ApiOperation(value = "获取文件夹树")
    public ApiResponse tree() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> element = new HashMap<>();
        element.put("title", "全部文件");
        element.put("id", null);
        element.put("children", getFolderTreeData(null));
        list.add(element);
        return ApiResponse.success(list);
    }

    /**
     * 获取文件夹树
     *
     * @param pid
     * @return
     */
    private List<Map<String, Object>> getFolderTreeData(Integer pid) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<UserFile> files = userFileService.findByPid(pid);
        for (UserFile f : files) {
            if (PublicConstants.DIR_CODE.equals(f.getIsDir())) {
                Map<String, Object> map = new HashMap<>();
                map.put("title", f.getFilename());
                map.put("id", f.getId());
                map.put("children", getFolderTreeData(f.getId()));
                list.add(map);
            }
        }
        return list;
    }

    @GetMapping("/capacity")
    @ApiOperation(value = "查询用户使用容量")
    public ApiResponse capacity() {
        SecurityUser user = ContextUtils.getCurrentUser();
        User user1 = userService.findById(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("capacity", Optional.ofNullable(user1.getCapacity()).orElse(0L));
        data.put("usedSize", Optional.ofNullable(user1.getUsedSize()).orElse(0L));
        return ApiResponse.success(data);
    }
}
