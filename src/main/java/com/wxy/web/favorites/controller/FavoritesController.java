package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.Password;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.CategoryService;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.service.PasswordService;
import com.wxy.web.favorites.util.*;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PasswordService passwordService;

    @Value("${index.page.size:10}")
    private Integer indexPageSize;

    @Value("${star.limit:10}")
    private Integer starLimit;

    @PostMapping("/save")
    public ApiResponse save(@RequestBody Favorites favorites) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        favorites.setUserId(user.getId());
        // 处理图标
        String icon = HtmlUtils.getIcon(favorites.getUrl());
        favorites.setIcon(StringUtils.isBlank(icon) ? "images/book.svg" : icon);
        // 拼音
        favorites.setPinyin(PinYinUtils.toPinyin(favorites.getName()));
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

    @PostMapping("/smartAdd")
    public ApiResponse smartAdd(@RequestBody Favorites favorites) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        favorites.setUserId(user.getId());
        // 设置分类
        Category category = categoryService.findDefaultCategory(user.getId());
        favorites.setCategoryId(category.getId());
        // 处理icon和title
        String icon = HtmlUtils.getIcon(favorites.getUrl());
        String title = HtmlUtils.getTitle(favorites.getUrl());
        favorites.setIcon(StringUtils.isBlank(icon) ? "images/book.svg" : icon);
        favorites.setName(StringUtils.isBlank(title) ? favorites.getUrl() : title);
        // 拼音
        favorites.setPinyin(PinYinUtils.toPinyin(favorites.getName()));
        favoritesService.save(favorites);
        return ApiResponse.success();
    }

    /**
     * 用户收藏列表(分页查询)
     *
     * @param pageNum
     * @return
     */
    @GetMapping("/list")
    public ApiResponse list(@RequestParam Integer pageNum) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        // 查询用户分类
        PageInfo<Category> page = categoryService.findPageByUserId(user.getId(), pageNum, indexPageSize);
        for (Category c : page.getList()) {
            c.setFavorites(favoritesService.findTop40ByCategoryIdOrderBySortDescIdAsc(c.getId()));
        }
        return ApiResponse.success(page);
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

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        favoritesService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Favorites favorites = favoritesService.findById(id);
        return ApiResponse.success(favorites);
    }

    @GetMapping("/search")
    public ApiResponse search(@RequestParam String name) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        name = Optional.ofNullable(name).orElse("").trim().toLowerCase();// 转换小写搜索
        List<Favorites> list = favoritesService.findTop100ByUserIdAndNameLikeOrPinyinLike(user.getId(),
                "%" + name + "%", "%" + name + "%");
        return ApiResponse.success(list);
    }

    @GetMapping("/star")
    public ApiResponse star() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        return ApiResponse.success(favoritesService.findStarFavorites(user.getId()));
    }

    @PostMapping("/star")
    public ApiResponse starUpdate(@RequestBody Favorites favorites) {
        Favorites favorites1 = favoritesService.findById(favorites.getId());
        if (favorites1 != null) {
            if (favorites.getStar() == 1) {
                User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
                List<Favorites> list = favoritesService.findStarFavorites(user.getId());
                if (list.size() >= starLimit && !list.contains(favorites1)) {
                    return ApiResponse.error("最多标记" + starLimit + "个网址");
                }
            }
            favorites1.setStar(favorites.getStar());
            favoritesService.save(favorites1);
            return ApiResponse.success();
        }
        return ApiResponse.error("非法操作");
    }

    private List<Category> parseXML(InputStream in) throws DocumentException {
        ArrayList<Category> list = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(in);
        Element root = document.getRootElement();
        root.elements("CATEGORY").forEach(c -> {
            ArrayList<Favorites> list1 = new ArrayList<>();
            c.element("LIST").elements("FAVORITES").forEach(f -> {
                Favorites favorites = new Favorites(null, f.elementText("NAME"), f.elementText("ICON"), f.elementText("URL"), null, null, PinYinUtils.toPinyin(f.elementText("NAME")), null, null, null);
                Element pwd = f.element("USER");
                if (pwd != null) {
                    Password password = new Password(null, pwd.elementText("ACCOUNT"), pwd.elementText("PASSWORD"), null);
                    favorites.setPassword(password);
                }
                list1.add(favorites);
            });
            list.add(new Category(null, c.elementText("NAME"), null, null, null, list1));
        });
        return list;
    }

    @PostMapping("/import")
    public ApiResponse upload(@RequestParam("file") MultipartFile file) throws IOException, DocumentException {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        if (file.getSize() > 0 && file.getOriginalFilename().endsWith(".xml")) {
            List<Category> list = parseXML(file.getInputStream());
            // 查询用户已存在的数据，防止重复导入
            List<Category> categories = categoryService.findByUserId(user.getId());
            for (Category c : categories) {
                List<Favorites> favoritesList = favoritesService.findByCategoryId(c.getId());
                for (Favorites f : favoritesList) {
                    Password password = passwordService.findByFavoritesId(f.getId());
                    f.setPassword(password);
                }
                c.setFavorites(favoritesList);
            }
            // 遍历导入数据
            for (Category c : list) {
                Category category = existCategory(c.getName(), categories);
                if (category == null) {// 如果该分类不存在，则新增分类，并保存所有收藏
                    c.setUserId(user.getId());
                    categoryService.save(c);
                    // 保存所有收藏
                    List<Favorites> favorites = c.getFavorites();
                    favorites.forEach(f -> {
                        f.setCategoryId(c.getId());
                        f.setUserId(user.getId());
                    });
                    favoritesService.saveAll(favorites);
                } else {// 如果该分类存在，则跳过分类，直接遍历收藏
                    for (Favorites f : c.getFavorites()) {
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
                    }
                }
            }
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
    public void export() throws IOException {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        // 查询用户分类
        List<Category> categories = categoryService.findByUserId(user.getId());
        for (Category c : categories) {
            List<Favorites> favoritesList = favoritesService.findByCategoryId(c.getId());
            for (Favorites f : favoritesList) {
                Password password = passwordService.findByFavoritesId(f.getId());
                f.setPassword(password);
            }
            c.setFavorites(favoritesList);
        }
        HttpServletResponse response = SpringUtils.getResponse();
        response.setContentType("application/force-download");// 设置强制下载不打开
        response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode("export.xml", "UTF-8"));// 设置文件名
        // 写入输出流
        writeXML(response.getOutputStream(), categories);
    }

    private void writeXML(OutputStream out, List<Category> categories) throws IOException {
        Document document = DocumentHelper.createDocument();
        Element data = document.addElement("DATA");
        categories.forEach(c -> {
            Element category = data.addElement("CATEGORY");
            category.addElement("NAME").setText(c.getName());
            Element list = category.addElement("LIST");
            c.getFavorites().forEach(f -> {
                Element favorites = list.addElement("FAVORITES");
                favorites.addElement("NAME").setText(f.getName());
                favorites.addElement("URL").setText(f.getUrl());
                favorites.addElement("ICON").setText(f.getIcon());
                if (f.getPassword() != null) {
                    Password password = f.getPassword();
                    Element pwd = favorites.addElement("USER");
                    pwd.addElement("ACCOUNT").setText(password.getAccount());
                    pwd.addElement("PASSWORD").setText(password.getPassword());
                }
            });
        });
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        XMLWriter writer = new XMLWriter(out, format);
        writer.setEscapeText(true);
        writer.write(document);
        writer.close();
    }
}
