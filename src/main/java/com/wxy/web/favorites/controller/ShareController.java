package com.wxy.web.favorites.controller;

import cn.hutool.core.lang.Assert;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.security.ContextUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/share")
@Api(tags = "书签库")
@Secured("share")
public class ShareController {

    @Autowired
    private FavoritesService favoritesService;

    @GetMapping("/list")
    @ApiOperation(value = "查询书签库")
    public ApiResponse list(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam(required = false) String name) {
        // 查询用户分类
        PageInfo<Favorites> page = favoritesService.findShareList(name, pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/support/{id}")
    @ApiOperation(value = "搜藏书签")
    public ApiResponse support(@PathVariable Integer id) {
        favoritesService.saveSupport(ContextUtils.getCurrentUser().getId(), id);
        return ApiResponse.success(favoritesService.findById(id).getSupport());
    }

    @GetMapping("/click/{id}")
    @ApiOperation(value = "点击书签")
    public ApiResponse click(@PathVariable Integer id) {
        Favorites favorites = favoritesService.findById(id);
        Assert.notNull(favorites, "书签不存在");
        favorites.setClickCount(favorites.getClickCount() + 1);
        favoritesService.save(favorites);
        return ApiResponse.success(favoritesService.findById(id).getClickCount());
    }
}
