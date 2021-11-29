package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.*;
import com.wxy.web.favorites.service.*;
import com.wxy.web.favorites.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private MomentService momentService;

    @Autowired
    private SearchTypeService searchTypeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SpringUtils springUtils;

    @PostMapping("/save")
    public ApiResponse save(@RequestBody Favorites favorites) {
        User user = springUtils.getCurrentUser();
        favorites.setUserId(user.getId());
        // 处理图标
        String icon = HtmlUtils.getIcon(favorites.getUrl());
        favorites.setIcon(StringUtils.isBlank(icon) ? PublicConstants.FAVORITES_ICON_DEFAULT : icon);
        // 拼音
        favorites.setPinyin(PinYinUtils.toPinyin(favorites.getName()));
        // 拼音首字母
        favorites.setPinyinS(PinYinUtils.toPinyinS(favorites.getName()));
        favoritesService.save(favorites);
        return ApiResponse.success();
    }

    @GetMapping("/url")
    public ApiResponse url(@RequestParam String url) {
        if (StringUtils.isNotBlank(url)) {
            return ApiResponse.success(HtmlUtils.getTitle(url));
        }
        return ApiResponse.error();
    }

    @GetMapping("/shortcut")
    public ApiResponse shortcut(@RequestParam String key) {
        User user = springUtils.getCurrentUser();
        Favorites favorites = favoritesService.findByShortcut(key, user.getId());
        if (favorites != null) {
            return ApiResponse.success(favorites);
        }
        return ApiResponse.error();
    }

    /**
     * 用户收藏列表(分页查询)
     *
     * @param pageNum
     * @return
     */
    @GetMapping("/list")
    public ApiResponse list(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        User user = springUtils.getCurrentUser();
        // 查询用户分类
        PageInfo<Category> page = categoryService.findPageByUserId(user.getId(), pageNum, pageSize);
        for (Category c : page.getList()) {
            c.setFavorites(favoritesService.findLimitByCategoryId(c.getId()));
        }
        return ApiResponse.success(page);
    }

    @GetMapping("/position")
    public ApiResponse position(@RequestParam Integer currentPage, @RequestParam Integer pageSize, @RequestParam Integer page) {
        User user = springUtils.getCurrentUser();
        List<Category> list = new ArrayList<>();
        int count = page - currentPage;
        int pageNum = currentPage + 1;
        for (int i = 0; i < count; i++) {
            // 查询用户分类
            List<Category> list1 = categoryService.findPageByUserId(user.getId(), pageNum++, pageSize).getList();
            for (Category c : list1) {
                c.setFavorites(favoritesService.findLimitByCategoryId(c.getId()));
            }
            list.addAll(list1);
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("currentPage", pageNum - 1);
        dataMap.put("list", list);
        return ApiResponse.success(dataMap);
    }

    /**
     * 显示更多
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/more")
    public ApiResponse more(@RequestParam Integer categoryId) {
        List<Favorites> favorites = favoritesService.findByCategoryId(categoryId);
        return ApiResponse.success(favorites);
    }

    /**
     * 查看回收站
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/recycle")
    public ApiResponse recycle(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        User user = springUtils.getCurrentUser();
        PageInfo<Favorites> page = favoritesService.findRecycleByPage(user.getId(), pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/shareList")
    public ApiResponse shareList(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        User user = springUtils.getCurrentUser();
        PageInfo<Favorites> page = favoritesService.findShareByPage(user.getId(), pageNum, pageSize);
        return ApiResponse.success(page);
    }

    /**
     * 清空回收站
     *
     * @return
     */
    @PostMapping("/recycle/clean")
    public ApiResponse CleanRecycle() {
        User user = springUtils.getCurrentUser();
        favoritesService.deleteAllFromRecycle(user.getId());
        return ApiResponse.success();
    }

    @PostMapping("/recycle/delete/{id}")
    public ApiResponse deleteFromRecycle(@PathVariable Integer id) {
        favoritesService.deleteById(id);
        return ApiResponse.success();
    }

    /**
     * 还原
     *
     * @param id
     * @return
     */
    @GetMapping("/recover/{id}")
    public ApiResponse recover(@PathVariable Integer id) {
        User user = springUtils.getCurrentUser();
        favoritesService.updateDeleteFlag(id, user.getId());
        return ApiResponse.success();
    }

    @GetMapping("/no-share/{id}")
    public ApiResponse noShare(@PathVariable Integer id) {
        favoritesService.updateShare(id);
        return ApiResponse.success();
    }

    /**
     * 逻辑删除
     *
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        Favorites favorites = favoritesService.findById(id);
        if (favorites != null) {
            favorites.setDeleteFlag(PublicConstants.DELETE_CODE);
            favorites.setDeleteTime(new Date());
            favoritesService.save(favorites);
        }
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Favorites favorites = favoritesService.findById(id);
        return ApiResponse.success(favorites);
    }

    @GetMapping("/search")
    public ApiResponse search(@RequestParam String name) {
        User user = springUtils.getCurrentUser();
        name = Optional.ofNullable(name).orElse("").trim().toLowerCase();// 转换小写搜索
        List<Favorites> favoritesList = favoritesService.findFavorites(user.getId(), name);
        List<Category> categoryList = categoryService.findCategories(user.getId(), name);
        Map<String, Object> data = new HashMap<>();
        data.put("favoritesList", favoritesList);
        data.put("categoryList", categoryList);
        return ApiResponse.success(data);
    }

    @GetMapping("/star")
    public ApiResponse star() {
        User user = springUtils.getCurrentUser();
        return ApiResponse.success(favoritesService.findStarFavorites(user.getId()));
    }

    @GetMapping("/visit/{id}")
    public ApiResponse visit(@PathVariable Integer id) {
        Favorites favorites = favoritesService.findById(id);
        if (favorites != null) {
            favorites.setVisitTime(new Date());
            favoritesService.save(favorites);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @PostMapping("/star")
    public ApiResponse starUpdate(@RequestBody Favorites favorites) {
        Favorites favorites1 = favoritesService.findById(favorites.getId());
        if (favorites1 != null) {
            if (favorites.getStar() == 1) {
                User user = springUtils.getCurrentUser();
                List<Favorites> list = favoritesService.findStarFavorites(user.getId());
                if (list.size() >= appConfig.getStarLimit() && !list.contains(favorites1)) {
                    return ApiResponse.error(PublicConstants.FAVORITES_STAR_LIMITED_MSG);
                }
            }
            favorites1.setStar(favorites.getStar());
            favoritesService.save(favorites1);
            return ApiResponse.success();
        }
        return ApiResponse.error(ErrorConstants.ILLEGAL_OPERATION_MSG);
    }

    @PostMapping("/share")
    public ApiResponse shareUpdate(@RequestBody Favorites favorites) {
        Favorites favorites1 = favoritesService.findById(favorites.getId());
        if (favorites1 != null) {
            favorites1.setIsShare(favorites.getIsShare());
            favoritesService.save(favorites1);
            return ApiResponse.success();
        }
        return ApiResponse.error(ErrorConstants.ILLEGAL_OPERATION_MSG);
    }

    private void parseMomentList(InputStream in) {
        Integer userId = springUtils.getCurrentUser().getId();
        try {
            List<Moment> list = new ArrayList<>();
            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            if (root.element("MOMENTS") != null) {
                root.element("MOMENTS").elements("MOMENT").forEach(m -> {
                    try {
                        String content = m.elementText("CONTENT");
                        String text = m.elementText("TEXT");
                        String time = m.elementText("TIME");
                        if (StringUtils.isNoneBlank(content) && StringUtils.isNoneBlank(time)) {
                            list.add(new Moment(null, content, text, userId, sdf.parse(time), null));
                        }
                    } catch (ParseException ignored) {
                    }
                });
            }
            momentService.saveAll(list);
        } catch (Exception e) {
            log.error("瞬间导入失败：userId = {}", userId, e);
        }
    }

    private void parseTaskList(InputStream in) {
        Integer userId = springUtils.getCurrentUser().getId();
        try {
            List<Task> list = new ArrayList<>();
            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
            SimpleDateFormat sdf1 = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            Date today = sdf.parse(sdf.format(new Date()));
            if (root.element("TASKS") != null) {
                root.element("TASKS").elements("TASK").forEach(t -> {
                    try {
                        Date date = sdf.parse(t.elementText("DATE"));
                        if (date.getTime() >= today.getTime()) {
                            list.add(new Task(null, t.elementText("CONTENT"), date, Boolean.parseBoolean(t.elementText("ALARM")) ? PublicConstants.TASK_ALARM_CODE : 0, sdf1.parse(t.elementText("TIME")), userId, null, Integer.valueOf(t.elementText("LEVEL"))));
                        }
                    } catch (ParseException ignored) {
                    }
                });
            }
            taskService.saveAll(list);
        } catch (Exception e) {
            log.error("日程导入失败：userId = {}", userId, e);
        }
    }

    private void parseSearchTypeList(InputStream in) {
        Integer userId = springUtils.getCurrentUser().getId();
        List<String> names = searchTypeService.findByUserId(userId).stream().map(SearchType::getName).collect(Collectors.toList());
        try {
            List<SearchType> list = new ArrayList<>();
            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();
            if (root.element("SEARCH_TYPES") != null) {
                root.element("SEARCH_TYPES").elements("SEARCH_TYPE").forEach(s -> {
                    String name = s.elementText("NAME");
                    if (!names.contains(name)) {
                        list.add(new SearchType(null, name, s.elementText("ICON"), s.elementText("URL"), userId));
                    }
                });
            }
            searchTypeService.saveAll(list);
        } catch (Exception e) {
            log.error("搜索导入失败：userId = {}", userId, e);
        }
    }

    private List<Category> parseCategoryList(InputStream in, Integer userId) {
        List<Category> list = new ArrayList<>();
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();
            if (root.element("CATEGORIES") != null) {
                root.element("CATEGORIES").elements("CATEGORY").forEach(c -> {
                    List<Favorites> list1 = new ArrayList<>();
                    c.element("LIST").elements("FAVORITES").forEach(f -> {
                        int sort = isInteger(f.elementText("SORT")) ? Integer.parseInt(f.elementText("SORT")) : -1;
                        Favorites favorites = new Favorites(null, f.elementText("NAME"), f.elementText("ICON"),
                                f.elementText("URL"), null, null, PinYinUtils.toPinyin(f.elementText("NAME")),
                                PinYinUtils.toPinyinS(f.elementText("NAME")),
                                StringUtils.isNotBlank(f.elementText("SHORTCUT")) ? f.elementText("SHORTCUT") : null,
                                StringUtils.isNotBlank(f.elementText("SCHEMA_NAME")) ? f.elementText("SCHEMA_NAME") : null,
                                sort >= 0 && sort < PublicConstants.MAX_SORT_NUMBER ? sort : null,
                                Boolean.parseBoolean(f.elementText("STAR")) ? PublicConstants.FAVORITES_STAR_CODE : null, null, null, null, Boolean.parseBoolean(f.elementText("SHARE")) ? PublicConstants.SHARE_CODE : null,  null, null,null);
                        Element pwd = f.element("USER");
                        if (pwd != null) {
                            Password password = new Password(null, pwd.elementText("ACCOUNT"), pwd.elementText("PASSWORD"), null);
                            favorites.setPassword(password);
                        }
                        list1.add(favorites);
                    });
                    int sort = isInteger(c.elementText("SORT")) ? Integer.parseInt(c.elementText("SORT")) : -1;
                    list.add(new Category(null, c.elementText("NAME"), null, null, sort >= 0 && sort < PublicConstants.MAX_SORT_NUMBER ? sort : null, Boolean.parseBoolean(c.elementText("BOOKMARK")) ? PublicConstants.BOOKMARK_STYLE_CODE : null, list1, null));
                });
            }
        } catch (Exception e) {
            log.error("收藏导入失败：userId = {}", userId, e);
        }
        return list;
    }

    @PostMapping("/import")
    public ApiResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        User user = springUtils.getCurrentUser();
        if (file.getSize() > 0 && Optional.ofNullable(file.getOriginalFilename()).orElse("").endsWith(".xml")) {
            List<Category> list = parseCategoryList(file.getInputStream(), user.getId());
            // 查询用户已存在的数据，防止重复导入
            List<Category> categories = categoryService.findByUserId(user.getId());
            categories.forEach(category -> {
                List<Favorites> favoritesList = favoritesService.findByCategoryId(category.getId());
                favoritesList.forEach(favorites -> {
                    Password password = passwordService.findByFavoritesId(favorites.getId());
                    favorites.setPassword(password);
                });
                category.setFavorites(favoritesList);
            });
            // 遍历导入数据
            list.forEach(c -> {
                Category category = existCategory(c.getName(), categories);
                if (category == null) {// 如果该分类不存在，则新增分类，并保存所有收藏
                    c.setUserId(user.getId());
                    categoryService.save(c);
                    // 保存所有收藏
                    Optional.ofNullable(c.getFavorites()).orElse(Collections.emptyList()).forEach(f -> {
                        f.setCategoryId(c.getId());
                        f.setUserId(user.getId());
                        Favorites favorites = favoritesService.save(f);
                        if (f.getPassword() != null) {
                            Password password = f.getPassword();
                            password.setFavoritesId(favorites.getId());
                            passwordService.save(password);
                        }
                    });
                } else {// 如果该分类存在，则跳过分类，直接遍历收藏
                    Optional.ofNullable(c.getFavorites()).orElse(Collections.emptyList()).forEach(f -> {
                        Favorites favorites = existFavorites(f.getUrl(), category.getFavorites());
                        if (favorites == null) {// 如果收藏不存在，则保存收藏
                            f.setCategoryId(category.getId());
                            f.setUserId(user.getId());
                            Favorites save = favoritesService.save(f);
                            // 保存密码
                            Password password = f.getPassword();
                            if (password != null) {
                                password.setFavoritesId(save.getId());
                                passwordService.save(password);
                            }
                        } else {// 如果收藏存在，则跳过收藏，看是否有密码需要新增
                            Password password = f.getPassword();
                            if (password != null && favorites.getPassword() == null) {
                                password.setFavoritesId(favorites.getId());
                                passwordService.save(password);
                            }
                        }
                    });
                }
            });
            // 保存瞬间
            parseMomentList(file.getInputStream());
            // 保存日程
            parseTaskList(file.getInputStream());
            // 保存搜索引擎
            parseSearchTypeList(file.getInputStream());
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    private Category existCategory(String name, List<Category> categories) {
        if (categories != null && categories.size() > 0) {
            for (Category c : categories) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }

    private Favorites existFavorites(String url, List<Favorites> favorites) {
        if (favorites != null && favorites.size() > 0) {
            for (Favorites f : favorites) {
                if (f.getUrl().equals(url)) {
                    return f;
                }
            }
        }
        return null;
    }

    @GetMapping("/export")
    public void export(@RequestParam(required = false) String f,
                       @RequestParam(required = false) String m,
                       @RequestParam(required = false) String t,
                       @RequestParam(required = false) String s) throws IOException, ParseException {
        List<Category> categories = new ArrayList<>();
        List<Moment> momentList = new ArrayList<>();
        List<Task> taskList = new ArrayList<>();
        List<SearchType> searchTypeList = new ArrayList<>();
        User user = springUtils.getCurrentUser();
        // 查询用户分类
        if (PublicConstants.EXPORT_FAVORITES_CODE.equals(f)) {
            categories = categoryService.findByUserId(user.getId());
            categories.forEach(category -> {
                List<Favorites> favoritesList = favoritesService.findByCategoryId(category.getId());
                favoritesList.forEach(favorites -> {
                    Password password = passwordService.findByFavoritesId(favorites.getId());
                    favorites.setPassword(password);
                });
                category.setFavorites(favoritesList);
            });
        }
        // 查询用户瞬间
        if (PublicConstants.EXPORT_MOMENT_CODE.equals(m)) {
            momentList = momentService.findByUserId(user.getId());
        }
        // 查询用户未完成的日程
        if (PublicConstants.EXPORT_TASK_CODE.equals(t)) {
            taskList = taskService.findUndoTaskByUserId(user.getId());
        }
        // 查询用户搜索引擎
        if (PublicConstants.EXPORT_SEARCH_CODE.equals(s)) {
            searchTypeList = searchTypeService.findByUserId(user.getId());
        }
        HttpServletResponse response = springUtils.getResponse();
        response.setContentType(PublicConstants.CONTENT_TYPE_STREAM);
        // 写入输出流
        writeXML(response.getOutputStream(), categories, momentList, taskList, searchTypeList);
    }

    private void writeXML(OutputStream out, List<Category> categories, List<Moment> momentList, List<Task> taskList, List<SearchType> searchTypeList) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("DATA");
        if (!CollectionUtils.isEmpty(categories)) {
            Element categoriesList = root.addElement("CATEGORIES");
            categories.forEach(c -> {
                Element category = categoriesList.addElement("CATEGORY");
                category.addElement("NAME").setText(c.getName());
                if (c.getSort() != null) {
                    category.addElement("SORT").setText(String.valueOf(c.getSort()));
                }
                if (PublicConstants.BOOKMARK_STYLE_CODE.equals(c.getBookmark())) {
                    category.addElement("BOOKMARK").setText("true");
                }
                Element list = category.addElement("LIST");
                Optional.ofNullable(c.getFavorites()).orElse(Collections.emptyList()).forEach(f -> {
                    Element favorites = list.addElement("FAVORITES");
                    favorites.addElement("NAME").setText(f.getName());
                    favorites.addElement("URL").setText(f.getUrl());
                    favorites.addElement("ICON").setText(f.getIcon());
                    if (f.getSort() != null) {
                        favorites.addElement("SORT").setText(String.valueOf(f.getSort()));
                    }
                    if (PublicConstants.FAVORITES_STAR_CODE.equals(f.getStar())) {
                        favorites.addElement("STAR").setText("true");
                    }
                    if (PublicConstants.SHARE_CODE.equals(f.getIsShare())) {
                        favorites.addElement("SHARE").setText("true");
                    }
                    if (StringUtils.isNotBlank(f.getShortcut())) {
                        favorites.addElement("SHORTCUT").setText(f.getShortcut());
                    }
                    if (StringUtils.isNotBlank(f.getSchemaName())) {
                        favorites.addElement("SCHEMA_NAME").setText(f.getSchemaName());
                    }
                    if (f.getPassword() != null) {
                        Password password = f.getPassword();
                        Element pwd = favorites.addElement("USER");
                        pwd.addElement("ACCOUNT").setText(password.getAccount());
                        pwd.addElement("PASSWORD").setText(password.getPassword());
                    }
                });
            });
        }
        if (!CollectionUtils.isEmpty(momentList)) {
            Element moments = root.addElement("MOMENTS");
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            momentList.forEach(m -> {
                Element moment = moments.addElement("MOMENT");
                moment.addElement("CONTENT").setText(m.getContent());
                if (StringUtils.isNotBlank(m.getText())) {
                    moment.addElement("TEXT").setText(m.getText());
                }
                moment.addElement("TIME").setText(sdf.format(m.getCreateTime()));
            });
        }
        if (!CollectionUtils.isEmpty(searchTypeList)) {
            Element searchTypes = root.addElement("SEARCH_TYPES");
            searchTypeList.forEach(s -> {
                Element searchType = searchTypes.addElement("SEARCH_TYPE");
                searchType.addElement("NAME").setText(s.getName());
                searchType.addElement("URL").setText(s.getUrl());
                searchType.addElement("ICON").setText(s.getIcon());
            });
        }
        if (!CollectionUtils.isEmpty(taskList)) {
            Element tasks = root.addElement("TASKS");
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
            SimpleDateFormat sdf1 = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            taskList.forEach(t -> {
                Element task = tasks.addElement("TASK");
                task.addElement("CONTENT").setText(t.getContent());
                task.addElement("DATE").setText(sdf.format(t.getTaskDate()));
                task.addElement("LEVEL").setText(String.valueOf(t.getLevel()));
                if (PublicConstants.TASK_ALARM_CODE.equals(t.getIsAlarm())) {
                    task.addElement("ALARM").setText("true");
                    task.addElement("TIME").setText(sdf1.format(t.getAlarmTime()));
                }
            });
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(StandardCharsets.UTF_8.name());
        XMLWriter writer = new XMLWriter(out, format);
        writer.setEscapeText(true);
        writer.write(document);
        writer.close();
    }

    private boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return StringUtils.isNotBlank(str) && pattern.matcher(str).matches();
    }
}
