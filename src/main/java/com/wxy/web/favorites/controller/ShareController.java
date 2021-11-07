package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PageInfo;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private SpringUtils springUtils;


    @GetMapping("/list")
    public ApiResponse list(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam(required = false) String name) {
        // 查询用户分类
        PageInfo<Favorites> page = favoritesService.findShareList(name, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/support/{id}")
    public ApiResponse support(@PathVariable Integer id) {
        boolean success = favoritesService.saveSupport(springUtils.getCurrentUser().getId(), id);
        return success ? ApiResponse.success() : ApiResponse.error();
    }
}
