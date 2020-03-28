package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @PostMapping
    public ApiResponse save(@RequestBody Category category) {
        if (!"默认分类".equals(category.getName())) {
            User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
            category.setUserId(user.getId());
            categoryRepository.save(category);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        return ApiResponse.success(category);
    }

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        Category category = categoryRepository.findById(id).get();
        if (!Integer.valueOf(1).equals(category.getIsSystem())) {
            categoryRepository.deleteById(id);
            favoritesRepository.deleteAll(favoritesRepository.findByCategoryId(id));
            return ApiResponse.success();
        }
        return ApiResponse.error("无法删除");
    }

    @PostMapping("/clean")
    public ApiResponse clean(@RequestParam Integer id) {
        favoritesRepository.deleteAll(favoritesRepository.findByCategoryId(id));
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse list() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        List<Category> list = categoryRepository.findByUserIdOrderBySortDesc(user.getId());
        return ApiResponse.success(list);
    }
}
