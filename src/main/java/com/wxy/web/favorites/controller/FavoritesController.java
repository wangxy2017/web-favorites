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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
        String icon = "/images/default.png";
        try {
            icon = HtmlUtils.getIcon(favorites.getUrl());
        } catch (Exception ignored) {
        }
        favorites.setIcon(icon);
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
        // 处理icon和name
        String icon = "/images/default.png";
        try {
            icon = HtmlUtils.getIcon(favorites.getUrl());
        } catch (Exception ignored) {
        }
        favorites.setIcon(icon);
        String name = favorites.getUrl();
        try {
            name = HtmlUtils.getTitle(favorites.getUrl());
        } catch (Exception ignored) {
        }
        favorites.setName(name);
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
}
