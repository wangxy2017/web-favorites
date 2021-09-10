package com.wxy.web.favorites.controller;

import cn.hutool.core.lang.Assert;
import com.sun.istack.NotNull;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import com.wxy.web.favorites.service.UserFileService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PageInfo;
import com.wxy.web.favorites.util.SpringUtils;
import com.wxy.web.favorites.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private UserService userService;

    @Autowired
    private SpringUtils springUtils;

    @Autowired
    private AppConfig appConfig;

    @GetMapping("/count")
    public ApiResponse count() {
        User user = springUtils.getCurrentUser();
        List<UserFile> list = userFileService.findRootList(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("count", list.size());
        return ApiResponse.success(data);
    }

    @GetMapping("/exists/{id}")
    public ApiResponse exists(@PathVariable Integer id) {
        UserFile file = userFileService.findById(id);
        if (file != null && StringUtils.isNotBlank(file.getPath()) && new File(file.getPath()).exists()) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/share/{id}")
    public ApiResponse share(@PathVariable Integer id) {
        UserFile file = userFileService.findById(id);
        if (file != null && StringUtils.isNotBlank(file.getPath()) && new File(file.getPath()).exists()) {
            if (StringUtils.isBlank(file.getShareId())) {
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
    public ApiResponse list(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer pid,
                            @RequestParam(required = false) Integer pageNum,
                            @RequestParam(required = false) Integer pageSize) {
        User user = springUtils.getCurrentUser();
        PageInfo<UserFile> page = userFileService.findPageList(user.getId(), name, pid, pageNum, pageSize);
        List<UserFile> floors = userFileService.findFloorsByPid(pid);
        HashMap<String, Object> data = new HashMap<>();
        data.put("parent", pid);
        data.put("page", page);
        data.put("floors", floors);
        return ApiResponse.success(data);
    }

    @PostMapping("/rename")
    public ApiResponse rename(@RequestParam Integer id, @RequestParam String filename) {
        UserFile file = userFileService.findById(id);
        if (file != null && StringUtils.isNoneBlank(filename)) {
            file.setFilename(filename);
            file.setUpdateTime(new Date());
            userFileService.save(file);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/share/download/{shareId}")
    public void shareDownload(HttpServletResponse response, @PathVariable String shareId) throws IOException {
        UserFile userFile = userFileService.findByShareId(shareId);
        Assert.notNull(userFile, "资源不存在");
        download(response, userFile.getId());
    }

    @GetMapping("/share/cancel/{id}")
    public ApiResponse shareCancel(HttpServletResponse response, @PathVariable Integer id) {
        UserFile userFile = userFileService.findById(id);
        if (userFile != null) {
            userFile.setShareId(null);
            userFileService.save(userFile);
        }
        return ApiResponse.success();
    }

    @GetMapping("/download/{id}")
    public void download(HttpServletResponse response, @PathVariable Integer id) throws IOException {
        UserFile userFile = userFileService.findById(id);
        Assert.notNull(userFile, "资源不存在");
        Assert.isTrue(!PublicConstants.DIR_CODE.equals(userFile.getIsDir()) && StringUtils.isNotBlank(userFile.getPath()), "数据异常");
        File file = new File(userFile.getPath());
        Assert.isTrue(file.exists(), "文件被物理删除");
        response.setContentType(PublicConstants.CONTENT_TYPE_STREAM);
        response.addHeader("Content-Disposition", "attachment;fileName=" + new String(userFile.getFilename().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        OutputStream out = response.getOutputStream();
        byte[] buf = new byte[1024 * 1024 * 10];
        int len;
        while ((len = bis.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        bis.close();
        out.close();
    }

    @GetMapping("/downloadAll")
    public void downloadAll(HttpServletResponse response) throws IOException {
        User user = springUtils.getCurrentUser();
        String tempPath = springUtils.getRequest().getServletContext().getRealPath("/");
        File file = userFileService.packageFile(user.getId(), tempPath);
        Assert.notNull(file, "资源不存在");
        ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
        out.setMethod(ZipEntry.DEFLATED);
        out.setLevel(appConfig.getFileCompressLevel());
        // 压缩文件
        ZipUtils.compressFile(out, file);
        // 删除临时文件
        file.delete();
    }

    @PostMapping("/upload")
    public ApiResponse upload(@RequestParam("file") MultipartFile[] files, @RequestParam(required = false) Integer pid) throws IOException {
        User user = springUtils.getCurrentUser();
        if (Optional.ofNullable(user.getCapacity()).orElse(0L) > 0) {
            long restSize = user.getCapacity() - Optional.ofNullable(user.getUsedSize()).orElse(0L);
            long totalSize = 0;
            for (MultipartFile file : files) {
                totalSize += file.getSize();
            }
            if (restSize > totalSize) {
                for (MultipartFile file : files) {
                    String path = userFileService.saveFile(file.getInputStream());
                    String filename = Objects.requireNonNull(file.getOriginalFilename()).replaceAll(" ", "+");
                    UserFile userFile = new UserFile(null, user.getId(), pid, new Date(), new Date(), filename, path, null, null, file.getSize(), null);
                    userFileService.save(userFile);
                    long newSize = Optional.ofNullable(user.getUsedSize()).orElse(0L) + file.getSize();
                    user.setUsedSize(newSize > user.getCapacity() ? user.getCapacity() : newSize);// 容量误差修正
                    userService.save(user);
                }
                return ApiResponse.success();
            }
        }
        return ApiResponse.error(ErrorConstants.NO_SPACE_LEFT_MSG);
    }

    @GetMapping("/back")
    public ApiResponse goBack(@RequestParam(required = false) Integer pid) {
        return ApiResponse.success(Optional.ofNullable(userFileService.findById(pid)).map(UserFile::getPid).orElse(null));
    }

    @PostMapping("/delete")
    public ApiResponse delete(@RequestParam Integer id) {
        User user = springUtils.getCurrentUser();
        userFileService.deleteById(id, user.getId());
        return ApiResponse.success();
    }

    @PostMapping("/deleteMore")
    public ApiResponse deleteMore(@RequestParam String ids) {
        User user = springUtils.getCurrentUser();
        String[] split = ids.split(PublicConstants.ID_DELIMITER);
        for (String s : split) {
            userFileService.deleteById(Integer.valueOf(s), user.getId());
        }
        return ApiResponse.success();
    }

    @PostMapping("/folder")
    public ApiResponse newFolder(@RequestParam String filename, @RequestParam(required = false) Integer pid) {
        User user = springUtils.getCurrentUser();
        UserFile file = new UserFile(null, user.getId(), pid, new Date(), new Date(), filename, null, null, 1, 0L, null);
        userFileService.save(file);
        return ApiResponse.success();
    }

    /**
     * 移动文件
     *
     * @return
     */
    @PostMapping("/move")
    public ApiResponse move(@RequestParam String ids, @RequestParam(required = false) Integer pid) {
        for (String id : ids.split(PublicConstants.ID_DELIMITER)) {
            UserFile file = userFileService.findById(Integer.valueOf(id));
            file.setPid(pid);
            userFileService.save(file);
        }
        return ApiResponse.success();
    }

    @GetMapping("/view")
    public ApiResponse view(@RequestParam Integer id) {
        UserFile file = userFileService.findById(id);
        if (file != null) {
            String suffix = file.getFilename().lastIndexOf(".") > -1 ? file.getFilename().substring(file.getFilename().lastIndexOf(".") + 1) : "";
            if (StringUtils.isNotBlank(suffix) && Optional.ofNullable(appConfig.getFileSuffixes()).orElse("").contains(suffix)) {
                StringBuilder sb = new StringBuilder();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
                    String tempStr;
                    while ((tempStr = reader.readLine()) != null) {
                        sb.append(tempStr).append("\n");
                    }
                } catch (IOException e) {
                    sb.append(ErrorConstants.FILE_READ_FAILED_MSG);
                    log.error("文件读取异常", e);
                }
                return ApiResponse.success(sb.toString());
            }
        }
        return ApiResponse.error();
    }

    @GetMapping("/tree")
    public ApiResponse tree() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<UserFile> root = userFileService.findRootList(springUtils.getCurrentUser().getId());
        for (UserFile file : root) {
            Map<String, Object> element = new HashMap<>();
            element.put("title", file.getFilename());
            element.put("id", file.getId());
            element.put("children", getFolderTreeData(file.getId()));
            list.add(element);
        }
        List<Map<String, Object>> list1 = new ArrayList<>();
        Map<String, Object> element = new HashMap<>();
        element.put("title", "全部文件");
        element.put("id", null);
        element.put("children", list);
        list1.add(element);
        return ApiResponse.success(list1);
    }

    /**
     * 获取文件夹树
     *
     * @param pid
     * @return
     */
    private List<Map<String, Object>> getFolderTreeData(Integer pid) {
        Assert.isNull(pid, "pid不能为null");
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
    public ApiResponse capacity() {
        User user = springUtils.getCurrentUser();
        User user1 = userService.findById(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("capacity", Optional.ofNullable(user1.getCapacity()).orElse(0L));
        data.put("usedSize", Optional.ofNullable(user1.getUsedSize()).orElse(0L));
        return ApiResponse.success(data);
    }
}
