package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import com.wxy.web.favorites.service.UserFileService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import com.wxy.web.favorites.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
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
        if (file!= null && StringUtils.isNotBlank(file.getPath())) {
            File disk = new File(file.getPath());
            if (disk.exists()) {
                return ApiResponse.success();
            }
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
    public ApiResponse list(@RequestParam(required = false) String name, @RequestParam(required = false) Integer pid) {
        User user = springUtils.getCurrentUser();
        List<UserFile> list;
        if (StringUtils.isNoneBlank(name)) {
            list = userFileService.searchFiles(user.getId(), "%" + name + "%");
        } else {
            if (pid == null) {
                list = userFileService.findRootList(user.getId());
            } else {
                list = userFileService.findByPid(pid);
            }
        }
        // 排序
        list.sort(Comparator.comparing(UserFile::getFilename));
        HashMap<String, Object> data = new HashMap<>();
        data.put("parent", pid);
        data.put("list", list);
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

    @GetMapping("/download/{id}")
    public void download(HttpServletResponse response, @PathVariable Integer id) {
        try {
            UserFile userFile = userFileService.findById(id);
            if (userFile != null && !PublicConstants.DIR_CODE.equals(userFile.getIsDir())) {
                File file = new File(userFile.getPath());
                if (file.exists()) {
                    response.setContentType(PublicConstants.CONTENT_TYPE_STREAM);
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
            }
        } catch (IOException e) {
            log.error("文件下载失败", e);
        }
    }

    @GetMapping("/downloadAll")
    public void downloadAll(HttpServletResponse response) {
        try {
            User user = springUtils.getCurrentUser();
            String tempPath = springUtils.getRequest().getServletContext().getRealPath("/");
            File file = userFileService.packageFileByUserId(user.getId(), tempPath);
            if (file != null) {
                ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
                out.setMethod(ZipEntry.DEFLATED);
                out.setLevel(7);
                // 压缩文件
                ZipUtils.compressFile(out, file);
                // 删除临时文件
                file.delete();
            }
        } catch (IOException e) {
            log.error("文件备份失败", e);
        }
    }

    @PostMapping("/upload")
    public ApiResponse upload(@RequestParam("file") MultipartFile file, @RequestParam(required = false) Integer pid) throws IOException {
        User user = springUtils.getCurrentUser();
        User user1 = userService.findById(user.getId());
        long restSize = Optional.ofNullable(user1.getCapacity()).orElse(0L) - Optional.ofNullable(user1.getUsedSize()).orElse(0L);
        if (restSize > file.getSize()) {
            String path = userFileService.writeFile(file.getInputStream());
            String filename = Objects.requireNonNull(file.getOriginalFilename()).replaceAll(" ", "+");
            UserFile userFile = new UserFile(null, user1.getId(), pid, new Date(), new Date(), filename, path, null, file.getSize(), null);
            userFileService.save(userFile);
            user1.setUsedSize(Optional.ofNullable(user1.getUsedSize()).orElse(0L) + file.getSize());
            userService.save(user1);
            return ApiResponse.success();
        }
        return ApiResponse.error(ErrorConstants.NO_SPACE_LEFT_MSG);
    }

    @GetMapping("/back")
    public ApiResponse goBack(@RequestParam(required = false) Integer pid) {
        Integer data = null;
        if (pid != null) {
            UserFile file = userFileService.findById(pid);
            if (file != null) {
                data = file.getPid();
            }
        }
        return ApiResponse.success(data);
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
        UserFile file = new UserFile(null, user.getId(), pid, new Date(), new Date(), filename, null, 1, 0L, null);
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
            if (StringUtils.isNotBlank(suffix) && appConfig.getFileSuffixes().contains(suffix)) {
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
        // 根目录
        Map<String, Object> map = new HashMap<>();
        map.put("title", "全部文件");
        map.put("id", null);
        map.put("children", getFolderTreeData(null));
        list.add(map);
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
    public ApiResponse capacity() {
        User user = springUtils.getCurrentUser();
        User user1 = userService.findById(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("capacity", Optional.ofNullable(user1.getCapacity()).orElse(0L));
        data.put("usedSize", Optional.ofNullable(user1.getUsedSize()).orElse(0L));
        return ApiResponse.success(data);
    }
}
