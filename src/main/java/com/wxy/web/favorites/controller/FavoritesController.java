package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.CategoryService;
import com.wxy.web.favorites.service.FavoritesService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
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

    @Value("${index.page.size:10}")
    private Integer indexPageSize;

    @PostMapping("/save")
    public ApiResponse save(@RequestBody Favorites favorites) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        favorites.setUserId(user.getId());
        // 处理图标
        String icon = null;
        try {
            icon = HtmlUtils.getIcon(favorites.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        favorites.setIcon(StringUtils.isBlank(icon) ? "/images/book.svg" : icon);
        // 拼音
        favorites.setPinyin(PinYinUtils.toPinyin(favorites.getName()));
        favoritesService.save(favorites);
        return ApiResponse.success();
    }

    @PostMapping("/smartAdd")
    public ApiResponse smartAdd(@RequestBody Favorites favorites) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        favorites.setUserId(user.getId());
        // 设置分类
        Category category = categoryService.findDefaultCategory(user.getId());
        favorites.setCategoryId(category.getId());
        // 处理icon和title
        String icon = null;
        String title = null;
        try {
            icon = HtmlUtils.getIcon(favorites.getUrl());
            title = HtmlUtils.getTitle(favorites.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        favorites.setIcon(StringUtils.isBlank(icon) ? "/images/book.svg" : title);
        favorites.setName(StringUtils.isBlank(title) ? favorites.getUrl() : icon);
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
            c.setFavorites(favoritesService.findTop40ByCategoryId(c.getId()));
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

    private List<Category> parseXML(InputStream in) throws DocumentException {
        ArrayList<Category> list = new ArrayList<>();
        SAXReader reader = new SAXReader();
        Document document = reader.read(in);
        Element root = document.getRootElement();
        root.elements("CATEGORY").forEach(c -> {
            ArrayList<Favorites> list1 = new ArrayList<>();
            c.element("LIST").elements("FAVORITES").forEach(f -> {
                list1.add(new Favorites(null, f.elementText("NAME"), f.elementText("ICON"), f.elementText("URL"), null, null, PinYinUtils.toPinyin(f.elementText("NAME"))));
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
            // 查询用户分类
            List<Category> categories = categoryService.findByUserId(user.getId());
            for (Category c : categories) {
                c.setFavorites(favoritesService.findByCategoryId(c.getId()));
            }
            // 遍历导入数据
            for (Category c : list) {
                // 如果分类不存在则新增
                Category category = existCategory(c.getName(), categories);
                if (category == null) {
                    c.setUserId(user.getId());
                    categoryService.save(c);
                    // 保存所有书签
                    List<Favorites> favorites = c.getFavorites();
                    favorites.forEach(f -> {
                        f.setCategoryId(c.getId());
                        f.setUserId(user.getId());
                    });
                    favoritesService.saveAll(favorites);
                } else {
                    // 遍历书签，不存在则保存
                    for (Favorites f : c.getFavorites()) {
                        Favorites favorites = existFavorites(f.getUrl(), category.getFavorites());
                        if (favorites == null) {
                            f.setCategoryId(category.getId());
                            f.setUserId(user.getId());
                            favoritesService.save(f);
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
            c.setFavorites(favoritesService.findByCategoryId(c.getId()));
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
