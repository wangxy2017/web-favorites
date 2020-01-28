package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @PostMapping
    public ApiResponse save(@RequestBody Category category) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        category.setUserId(user.getId());
        categoryRepository.save(category);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Category category = categoryRepository.getOne(id);
        Category category1 = new Category();
        BeanUtils.copyProperties(category, category1);
        return ApiResponse.success(category1);
    }

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        categoryRepository.deleteById(id);
        return ApiResponse.success();
    }
}
