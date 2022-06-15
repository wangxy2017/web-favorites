package com.wxy.web.favorites.controller.user;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.*;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.*;
import com.wxy.web.favorites.util.HtmlUtils;
import com.wxy.web.favorites.util.PinYinUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
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
@Api(tags = "收藏")
@Secured("USER")
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
    private QuickNavigationService quickNavigationService;

    @Autowired
    private MemorandumService memorandumService;

    @Autowired
    private UserService userService;

    @PostMapping("/save")
    @ApiOperation(value = "保存书签")
    public ApiResponse save(@RequestBody Favorites favorites) {
        if (favorites.getId() == null) {// 新增
            Assert.notNull(favorites.getCategoryId(), "分类不能为空");
            SecurityUser user = ContextUtils.getCurrentUser();
            favorites.setUserId(user.getId());
            // 处理图标
            String icon = HtmlUtils.getIcon(favorites.getUrl());
            favorites.setIcon(StrUtil.isBlank(icon) ? PublicConstants.FAVORITES_ICON_DEFAULT : icon);
            // 拼音
            favorites.setPinyin(PinYinUtils.toPinyin(favorites.getName()));
            // 拼音首字母
            favorites.setPinyinS(PinYinUtils.toPinyinS(favorites.getName()));
            favoritesService.save(favorites);
        } else {// 修改
            Favorites favorites1 = favoritesService.findById(favorites.getId());
            Assert.notNull(favorites1, "书签不存在");
            favorites1.setUrl(favorites.getUrl());
            favorites1.setName(favorites.getName());
            // 处理图标
            String icon = HtmlUtils.getIcon(favorites.getUrl());
            favorites1.setIcon(StrUtil.isBlank(icon) ? PublicConstants.FAVORITES_ICON_DEFAULT : icon);
            // 拼音
            favorites1.setPinyin(PinYinUtils.toPinyin(favorites.getName()));
            // 拼音首字母
            favorites1.setPinyinS(PinYinUtils.toPinyinS(favorites.getName()));
            favorites1.setCategoryId(favorites.getCategoryId());
            favorites1.setSort(favorites.getSort());
            favorites1.setStar(favorites.getStar());
            favorites1.setSchemaName(favorites.getSchemaName());
            favorites1.setShortcut(favorites.getShortcut());
            favorites1.setIsShare(favorites.getIsShare());
            favoritesService.save(favorites1);
        }
        return ApiResponse.success();
    }

    @GetMapping("/url")
    @ApiOperation(value = "获取标题")
    public ApiResponse url(@RequestParam String url) {
        if (StrUtil.isNotBlank(url)) {
            return ApiResponse.success(HtmlUtils.getTitle(url));
        }
        return ApiResponse.error();
    }

    @GetMapping("/shortcut")
    @ApiOperation(value = "根据快捷口令查询书签")
    public ApiResponse shortcut(@RequestParam String key) {
        SecurityUser user = ContextUtils.getCurrentUser();
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
    @ApiOperation(value = "用户收藏列表(分页查询)")
    public ApiResponse list(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        SecurityUser user = ContextUtils.getCurrentUser();
        // 查询用户分类
        PageInfo<Category> page = categoryService.findPageByUserId(user.getId(), pageNum, pageSize);
        for (Category c : page.getList()) {
            c.setFavorites(favoritesService.findLimitByCategoryId(c.getId()));
        }
        return ApiResponse.success(page);
    }

    @GetMapping("/position/{categoryId}")
    @ApiOperation(value = "查询定位")
    public ApiResponse position(@PathVariable Integer categoryId) {
        Category category = categoryService.findById(categoryId);
        category.setFavorites(favoritesService.findLimitByCategoryId(category.getId()));
        return ApiResponse.success(category);
    }

    /**
     * 显示更多
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/more")
    @ApiOperation(value = "显示更多")
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
    @ApiOperation(value = "查看回收站")
    public ApiResponse recycle(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        SecurityUser user = ContextUtils.getCurrentUser();
        PageInfo<Favorites> page = favoritesService.findRecycleByPage(user.getId(), pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/shareList")
    @ApiOperation(value = "查询我的分享列表")
    public ApiResponse shareList(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        SecurityUser user = ContextUtils.getCurrentUser();
        PageInfo<Favorites> page = favoritesService.findShareByPage(user.getId(), pageNum, pageSize);
        return ApiResponse.success(page);
    }

    /**
     * 清空回收站
     *
     * @return
     */
    @PostMapping("/recycle/clean")
    @ApiOperation(value = "清空回收站")
    public ApiResponse CleanRecycle() {
        SecurityUser user = ContextUtils.getCurrentUser();
        favoritesService.deleteAllFromRecycle(user.getId());
        return ApiResponse.success();
    }

    @PostMapping("/recycle/delete/{id}")
    @ApiOperation(value = "回收站删除")
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
    @ApiOperation(value = "还原")
    public ApiResponse recover(@PathVariable Integer id) {
        SecurityUser user = ContextUtils.getCurrentUser();
        favoritesService.updateDeleteFlag(id, user.getId());
        return ApiResponse.success();
    }

    @GetMapping("/no-share/{id}")
    @ApiOperation(value = "取消分享")
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
    @ApiOperation(value = "逻辑删除")
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
    @ApiOperation(value = "根据id查询")
    public ApiResponse query(@PathVariable Integer id) {
        Favorites favorites = favoritesService.findById(id);
        return ApiResponse.success(favorites);
    }

    @GetMapping("/search")
    @ApiOperation(value = "搜索书签")
    public ApiResponse search(@RequestParam String name) {
        SecurityUser user = ContextUtils.getCurrentUser();
        name = Optional.ofNullable(name).orElse("").trim().toLowerCase();// 转换小写搜索
        List<Favorites> favoritesList = favoritesService.findFavorites(user.getId(), name);
        List<Category> categoryList = categoryService.findCategories(user.getId(), name);
        Map<String, Object> data = new HashMap<>();
        data.put("favoritesList", favoritesList);
        data.put("categoryList", categoryList);
        return ApiResponse.success(data);
    }

    @GetMapping("/star")
    @ApiOperation(value = "查询常用网址")
    public ApiResponse star() {
        SecurityUser user = ContextUtils.getCurrentUser();
        return ApiResponse.success(favoritesService.findStarFavorites(user.getId()));
    }

    @GetMapping("/visit/{id}")
    @ApiOperation(value = "记录访问时间")
    public ApiResponse visit(@PathVariable Integer id) {
        Favorites favorites = favoritesService.findById(id);
        if (favorites != null) {
            favorites.setVisitTime(new Date());
            favoritesService.save(favorites);
            SecurityUser securityUser = ContextUtils.getCurrentUser();
            User user = userService.findById(securityUser.getId());
            user.setClickCount(Optional.ofNullable(user.getClickCount()).orElse(0) + 1);
            userService.save(user);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @PostMapping("/star")
    @ApiOperation(value = "标记为常用")
    public ApiResponse starUpdate(@RequestBody Favorites favorites) {
        Favorites favorites1 = favoritesService.findById(favorites.getId());
        if (favorites1 != null) {
            if (favorites.getStar() == 1) {
                SecurityUser user = ContextUtils.getCurrentUser();
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
    @ApiOperation(value = "分享书签")
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
        Integer userId = ContextUtils.getCurrentUser().getId();
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
                        if (StrUtil.isNotBlank(content) && StrUtil.isNotBlank(time)) {
                            list.add(new Moment().setContent(content).setText(text).setUserId(userId).setCreateTime(sdf.parse(time)));
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
        Integer userId = ContextUtils.getCurrentUser().getId();
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
                            list.add(new Task().setContent(t.elementText("CONTENT")).setTaskDate(date).setIsAlarm(Boolean.parseBoolean(t.elementText("ALARM")) ? PublicConstants.TASK_ALARM_CODE : 0).setAlarmTime(sdf1.parse(t.elementText("TIME"))).setUserId(userId).setLevel(Integer.valueOf(t.elementText("LEVEL"))));
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
        Integer userId = ContextUtils.getCurrentUser().getId();
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
                        list.add(new SearchType().setName(name).setIcon(s.elementText("ICON")).setUrl(s.elementText("URL")).setUserId(userId));
                    }
                });
            }
            searchTypeService.saveAll(list);
        } catch (Exception e) {
            log.error("搜索导入失败：userId = {}", userId, e);
        }
    }

    private void parseNavigationList(InputStream in) {
        Integer userId = ContextUtils.getCurrentUser().getId();
        List<String> urls = quickNavigationService.findByUserId(userId).stream().map(QuickNavigation::getUrl).collect(Collectors.toList());
        try {
            List<QuickNavigation> list = new ArrayList<>();
            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();
            if (root.element("NAVIGATIONS") != null) {
                root.element("NAVIGATIONS").elements("NAVIGATION").forEach(n -> {
                    String url = n.elementText("URL");
                    int sort = isInteger(n.elementText("SORT")) ? Integer.parseInt(n.elementText("SORT")) : -1;
                    if (!urls.contains(url)) {
                        list.add(new QuickNavigation().setName(n.elementText("NAME")).setIcon(n.elementText("ICON")).setUrl(url).setUserId(userId).setSort(sort > 0 ? sort : null));
                    }
                });
            }
            int count = appConfig.getNavigationLimit() - urls.size();
            for (int i = 0; i < count - 1; i++) {
                if (i < list.size()) {
                    quickNavigationService.save(list.get(i));
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("快捷导航导入失败：userId = {}", userId, e);
        }
    }

    private void parseMemorandumList(InputStream in) {
        Integer userId = ContextUtils.getCurrentUser().getId();
        try {
            List<Memorandum> list = new ArrayList<>();
            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            if (root.element("MEMORANDUMS") != null) {
                root.element("MEMORANDUMS").elements("MEMORANDUM").forEach(r -> {
                    String content = r.elementText("CONTENT");
                    String createTime = r.elementText("CREATE_TIME");
                    if (StrUtil.isNotBlank(content) && StrUtil.isNotBlank(createTime)) {
                        try {
                            list.add(new Memorandum().setContent(content).setUserId(userId).setCreateTime(sdf.parse(createTime)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            memorandumService.saveAll(list);
        } catch (Exception e) {
            log.error("备忘录导入失败：userId = {}", userId, e);
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
                        Favorites favorites = new Favorites().setName(f.elementText("NAME")).setIcon(f.elementText("ICON")).setUrl(f.elementText("URL")).setPinyin(PinYinUtils.toPinyin(f.elementText("NAME"))).setPinyinS(PinYinUtils.toPinyinS(f.elementText("NAME"))).setShortcut(StrUtil.isNotBlank(f.elementText("SHORTCUT")) ? f.elementText("SHORTCUT") : null).setSchemaName(StrUtil.isNotBlank(f.elementText("SCHEMA_NAME")) ? f.elementText("SCHEMA_NAME") : null).setSort(sort >= 0 && sort < PublicConstants.MAX_SORT_NUMBER ? sort : null).setStar(Boolean.parseBoolean(f.elementText("STAR")) ? PublicConstants.FAVORITES_STAR_CODE : null).setIsShare(Boolean.parseBoolean(f.elementText("SHARE")) ? PublicConstants.SHARE_CODE : null);
                        Element pwd = f.element("USER");
                        if (pwd != null) {
                            Password password = new Password().setAccount(pwd.elementText("ACCOUNT")).setPassword(pwd.elementText("PASSWORD"));
                            favorites.setPassword(password);
                        }
                        list1.add(favorites);
                    });
                    int sort = isInteger(c.elementText("SORT")) ? Integer.parseInt(c.elementText("SORT")) : -1;
                    list.add(new Category().setName(c.elementText("NAME")).setSort(sort >= 0 && sort < PublicConstants.MAX_SORT_NUMBER ? sort : null).setBookmark(Boolean.parseBoolean(c.elementText("BOOKMARK")) ? PublicConstants.BOOKMARK_STYLE_CODE : null).setPinyin( PinYinUtils.toPinyin(c.elementText("NAME"))).setPinyinS(c.elementText("NAME")).setFavorites(list1));
                });
            }
        } catch (Exception e) {
            log.error("收藏导入失败：userId = {}", userId, e);
        }
        return list;
    }

    @PostMapping("/import")
    @ApiOperation(value = "导入")
    public ApiResponse upload(@RequestParam("file") MultipartFile file) throws IOException {
        SecurityUser user = ContextUtils.getCurrentUser();
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
                            password.setUserId(user.getId());
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
                                password.setUserId(user.getId());
                                password.setFavoritesId(save.getId());
                                passwordService.save(password);
                            }
                        } else {// 如果收藏存在，则跳过收藏，看是否有密码需要新增
                            Password password = f.getPassword();
                            if (password != null && favorites.getPassword() == null) {
                                password.setUserId(user.getId());
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
            // 保存快捷导航
            parseNavigationList(file.getInputStream());
            // 保存备忘录
            parseMemorandumList(file.getInputStream());
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @PostMapping("/importHtml")
    @ApiOperation(value = "导入html")
    public ApiResponse importHtml(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.getSize() > 0 && Optional.ofNullable(file.getOriginalFilename()).orElse("").endsWith(".html")) {
            SecurityUser user = ContextUtils.getCurrentUser();
            Category category = categoryService.findDefaultCategory(user.getId());
            List<String> existsUrls = favoritesService.findByCategoryId(category.getId()).stream().map(Favorites::getUrl).collect(Collectors.toList());
            org.jsoup.nodes.Document document = Jsoup.parse(file.getInputStream(), StandardCharsets.UTF_8.name(), "");
            List<Favorites> favoritesList = document.getElementsByTag("a").stream().map(element -> {
                String url = element.attr("href");
                String name = element.text();
                if (StrUtil.isNotBlank(name) && !existsUrls.contains(url)) {
                    String icon = HtmlUtils.getIcon(url);
                    Favorites f = new Favorites();
                    f.setName(name);
                    f.setUrl(url);
                    f.setIcon(StrUtil.isNotBlank(icon) ? icon : PublicConstants.FAVORITES_ICON_DEFAULT);
                    f.setUserId(user.getId());
                    f.setPinyin(PinYinUtils.toPinyin(name));
                    f.setPinyinS(PinYinUtils.toPinyinS(name));
                    f.setCategoryId(category.getId());
                    return f;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            favoritesService.saveAll(favoritesList);
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
    @ApiOperation(value = "导出")
    public void export(@RequestParam(required = false) String favorites, @RequestParam(required = false) String moment, @RequestParam(required = false) String task, @RequestParam(required = false) String navigation, @RequestParam(required = false) String memorandum, @RequestParam(required = false) String search) throws IOException, ParseException {
        List<Category> categories = new ArrayList<>();
        List<Moment> momentList = new ArrayList<>();
        List<Task> taskList = new ArrayList<>();
        List<SearchType> searchTypeList = new ArrayList<>();
        List<QuickNavigation> quickNavigationList = new ArrayList<>();
        List<Memorandum> memorandumList = new ArrayList<>();
        SecurityUser user = ContextUtils.getCurrentUser();
        // 查询用户分类
        if (PublicConstants.EXPORT_FAVORITES_CODE.equals(favorites)) {
            categories = categoryService.findByUserId(user.getId());
            categories.forEach(category -> {
                List<Favorites> favoritesList = favoritesService.findByCategoryId(category.getId());
                favoritesList.forEach(f -> {
                    Password password = passwordService.findByFavoritesId(f.getId());
                    f.setPassword(password);
                });
                category.setFavorites(favoritesList);
            });
        }
        // 查询用户瞬间
        if (PublicConstants.EXPORT_MOMENT_CODE.equals(moment)) {
            momentList = momentService.findByUserId(user.getId());
        }
        // 查询用户未完成的日程
        if (PublicConstants.EXPORT_TASK_CODE.equals(task)) {
            taskList = taskService.findUndoTaskByUserId(user.getId());
        }
        // 查询用户搜索引擎
        if (PublicConstants.EXPORT_SEARCH_CODE.equals(search)) {
            searchTypeList = searchTypeService.findByUserId(user.getId());
        }
        // 查询快捷导航
        if (PublicConstants.EXPORT_QUICK_NAVIGATION.equals(navigation)) {
            quickNavigationList = quickNavigationService.findByUserId(user.getId());
        }
        // 查询备忘录
        if (PublicConstants.EXPORT_QUICK_NAVIGATION.equals(memorandum)) {
            memorandumList = memorandumService.findByUserId(user.getId());
        }
        HttpServletResponse response = ContextUtils.getResponse();
        response.setContentType(ContentType.OCTET_STREAM.getValue());
        // 写入输出流
        writeXML(response.getOutputStream(), categories, momentList, taskList, searchTypeList, quickNavigationList, memorandumList);
    }

    private void writeXML(OutputStream out, List<Category> categories, List<Moment> momentList, List<Task> taskList, List<SearchType> searchTypeList, List<QuickNavigation> quickNavigationList, List<Memorandum> memorandumList) throws IOException {
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
                    if (StrUtil.isNotBlank(f.getShortcut())) {
                        favorites.addElement("SHORTCUT").setText(f.getShortcut());
                    }
                    if (StrUtil.isNotBlank(f.getSchemaName())) {
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
                if (StrUtil.isNotBlank(m.getText())) {
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
        if (!CollectionUtils.isEmpty(quickNavigationList)) {
            Element navigations = root.addElement("NAVIGATIONS");
            quickNavigationList.forEach(n -> {
                Element navigation = navigations.addElement("NAVIGATION");
                navigation.addElement("NAME").setText(n.getName());
                navigation.addElement("URL").setText(n.getUrl());
                navigation.addElement("ICON").setText(n.getIcon());
                if (n.getSort() != null) {
                    navigation.addElement("SORT").setText(String.valueOf(n.getSort()));
                }
            });
        }
        if (!CollectionUtils.isEmpty(memorandumList)) {
            Element memorandums = root.addElement("MEMORANDUMS");
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            memorandumList.forEach(r -> {
                Element memorandum = memorandums.addElement("MEMORANDUM");
                memorandum.addElement("CONTENT").setText(r.getContent());
                memorandum.addElement("CREATE_TIME").setText(sdf.format(r.getCreateTime()));
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
        Pattern pattern = Pattern.compile("^[-+]?[\\d]*$");
        return StrUtil.isNotBlank(str) && pattern.matcher(str).matches();
    }
}
