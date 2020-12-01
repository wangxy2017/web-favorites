package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.CategoryService;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FavoritesService favoritesService;

    @PostMapping
    public ApiResponse save(@RequestBody Category category) {
        if (!"默认分类".equals(category.getName())) {
            User user = SpringUtils.getCurrentUser();
            category.setUserId(user.getId());
            categoryService.save(category);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Category category = categoryService.findById(id);
        return ApiResponse.success(category);
    }

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        Category category = categoryService.findById(id);
        if (!Integer.valueOf(1).equals(category.getIsSystem())) {
            categoryService.deleteById(id);
            favoritesService.deleteAll(favoritesService.findByCategoryId(id));
            return ApiResponse.success();
        }
        return ApiResponse.error("系统分类无法删除");
    }

    @PostMapping("/clean")
    public ApiResponse clean(@RequestParam Integer id) {
        favoritesService.deleteAll(favoritesService.findByCategoryId(id));
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse list() {
        User user = SpringUtils.getCurrentUser();
        List<Category> list = categoryService.findByUserId(user.getId());
        return ApiResponse.success(list);
    }

    @GetMapping("/catalog")
    public ApiResponse catalog() {
        User user = SpringUtils.getCurrentUser();
        List<Category> list = categoryService.findCatalog(user.getId());
        return ApiResponse.success(list);
    }
}
