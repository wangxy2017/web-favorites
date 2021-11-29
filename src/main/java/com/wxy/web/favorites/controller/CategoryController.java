package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.CategoryService;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/category")
@Api(tags = "分类管理")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private SpringUtils springUtils;

    @PostMapping
    @ApiOperation(value = "保存分类")
    public ApiResponse save(@RequestBody Category category) {
        if (!PublicConstants.DEFAULT_CATEGORY_NAME.equals(category.getName())) {
            User user = springUtils.getCurrentUser();
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

    @GetMapping("/check/{name}")
    public ApiResponse queryName(@PathVariable String name) {
        User user = springUtils.getCurrentUser();
        Category category = categoryService.findByName(name, user.getId());
        if (category != null) {
            return ApiResponse.success(category);
        }
        return ApiResponse.error();
    }

    @GetMapping("/favorites/{categoryId}")
    public ApiResponse favorites(@PathVariable Integer categoryId) {
        List<Favorites> favorites = favoritesService.findByCategoryId(categoryId);
        return ApiResponse.success(favorites);
    }

    /**
     * 删除分类
     *
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        Category category = categoryService.findById(id);
        if (!PublicConstants.SYSTEM_CATEGORY_CODE.equals(category.getIsSystem())) {
            categoryService.deleteById(id);
            // 收藏移动至回收站
            List<Favorites> favoritesList = favoritesService.findByCategoryId(id);
            favoritesList.forEach(favorites -> {
                favorites.setDeleteFlag(PublicConstants.DELETE_CODE);
                favorites.setDeleteTime(new Date());
            });
            favoritesService.saveAll(favoritesList);
            return ApiResponse.success();
        }
        return ApiResponse.error(ErrorConstants.SYSTEM_CATEGORY_NO_DELETE_MSG);
    }

    /**
     * 清空收藏
     *
     * @param id
     * @return
     */
    @PostMapping("/clean")
    public ApiResponse clean(@RequestParam Integer id) {
        List<Favorites> favoritesList = favoritesService.findByCategoryId(id);
        favoritesList.forEach(favorites -> {
            favorites.setDeleteFlag(PublicConstants.DELETE_CODE);
            favorites.setDeleteTime(new Date());
        });
        favoritesService.saveAll(favoritesList);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse list() {
        User user = springUtils.getCurrentUser();
        List<Category> list = categoryService.findByUserId(user.getId());
        return ApiResponse.success(list);
    }

    @PostMapping("/bookmark")
    public ApiResponse bookmark(@RequestBody Category category) {
        Category category1 = categoryService.findById(category.getId());
        if (category1 != null) {
            category1.setBookmark(category.getBookmark());
            categoryService.save(category1);
            return ApiResponse.success();
        }
        return ApiResponse.error(ErrorConstants.ILLEGAL_OPERATION_MSG);
    }
}
