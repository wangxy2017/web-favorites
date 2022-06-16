package com.wxy.web.favorites.controller.user;

import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.Password;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.service.PasswordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
@Api(tags = "密码")
@Secured("password")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private FavoritesService favoritesService;

    

    @PostMapping
    @ApiOperation(value = "保存密码")
    public ApiResponse save(@RequestBody Password password) {
        Favorites favorites = favoritesService.findById(password.getFavoritesId());
        if (favorites != null && (StrUtil.isNotBlank(password.getAccount()) || StrUtil.isNotBlank(password.getPassword()))) {
            password.setUserId(ContextUtils.getCurrentUser().getId());
            Password password1 = passwordService.save(password);
            return ApiResponse.success(password1.getId());
        }
        return ApiResponse.error();
    }

    @GetMapping("/fid/{fid}")
    @ApiOperation(value = "根据收藏id查询")
    public ApiResponse queryByFavoritesId(@PathVariable Integer fid) {
        Password password = passwordService.findByFavoritesId(fid);
        if (password != null) {
            return ApiResponse.success(password);
        }
        return ApiResponse.error();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除密码")
    public ApiResponse delete(@PathVariable Integer id) {
        passwordService.deleteById(id);
        return ApiResponse.success();
    }
}
