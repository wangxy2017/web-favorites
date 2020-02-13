package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.HtmlUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping("/save")
    public ApiResponse save(@RequestBody Favorites favorites) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        favorites.setUserId(user.getId());
        // 处理图标
        String icon = "";
        try {
            icon = HtmlUtils.getIcon(favorites.getUrl());
        } catch (Exception ignored) {
        }
        favorites.setIcon(StringUtils.isBlank(icon) ? "/images/default.png" : icon);
        favoritesRepository.save(favorites);
        return ApiResponse.success();
    }

    @PostMapping("/smartAdd")
    public ApiResponse smartAdd(@RequestBody Favorites favorites) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        favorites.setUserId(user.getId());
        // 设置分类
        Category category = categoryRepository.findDefaultCategory(user.getId());
        favorites.setCategoryId(category.getId());
        // 处理icon和title
        String icon = "";
        try {
            icon = HtmlUtils.getIcon(favorites.getUrl());
        } catch (Exception ignored) {
        }
        favorites.setIcon(StringUtils.isBlank(icon) ? "/images/default.png" : icon);
        String title = "";
        try {
            title = HtmlUtils.getTitle(favorites.getUrl());
        } catch (Exception ignored) {
        }
        favorites.setName(StringUtils.isBlank(title) ? favorites.getUrl() : title);
        favoritesRepository.save(favorites);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse list() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        // 查询用户分类
        List<Category> categories = categoryRepository.findByUserId(user.getId());
        for (Category c : categories) {
            c.setFavorites(favoritesRepository.findByCategoryId(c.getId()));
        }
        return ApiResponse.success(categories);
    }

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        favoritesRepository.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Favorites favorites = favoritesRepository.findById(id).orElse(null);
        return ApiResponse.success(favorites);
    }

    @GetMapping("/search")
    public ApiResponse search(@RequestParam String name) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        List<Favorites> list = favoritesRepository.findByNameLike(user.getId(), name);
        return ApiResponse.success(list);
    }

    @PostMapping("/upload")
    public ApiResponse upload(@RequestParam("file") MultipartFile file) throws IOException, DocumentException {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        if (file.getSize() > 0 && file.getOriginalFilename().endsWith(".xml")) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(file.getInputStream());
            Element root = document.getRootElement();
            root.elements("CATEGORY").forEach(c -> {
                Category category = new Category(null, c.elementText("NAME"), user.getId(), null, null);
                categoryRepository.save(category);
                c.elements("FAVORITES").forEach(f -> {
                    Favorites favorites = new Favorites(null, f.elementText("NAME"), f.elementText("ICON"), f.elementText("URL"), category.getId(), user.getId());
                    favoritesRepository.save(favorites);
                });
            });
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        // 查询用户分类
        List<Category> categories = categoryRepository.findByUserId(user.getId());
        for (Category c : categories) {
            c.setFavorites(favoritesRepository.findByCategoryId(c.getId()));
        }
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("DATA");
        root.addAttribute("version", "1.0");
        categories.forEach(c -> {
            Element element = root.addElement("CATEGORY");
            element.addElement("NAME").setText(c.getName());
            c.getFavorites().forEach(f -> {
                Element element1 = element.addElement("FAVORITES");
                element1.addElement("NAME").setText(f.getName());
                element1.addElement("URL").setText(f.getUrl());
                element1.addElement("ICON").setText(f.getIcon());
            });
        });
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        OutputStream outputStream = response.getOutputStream();
        XMLWriter writer = new XMLWriter(outputStream, format);
        writer.setEscapeText(false);
        writer.write(document);
        writer.close();
        response.setContentType("application/force-download");// 设置强制下载不打开
        response.addHeader("Content-Disposition", "attachment;fileName="
                + URLEncoder.encode("favorites_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))
                + ".xml", StandardCharsets.UTF_8.name()));// 设置文件名
    }
}
