package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.Password;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.service.PasswordService;
import com.wxy.web.favorites.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/password")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private FavoritesService favoritesService;

    @PostMapping
    public ApiResponse save(@RequestBody Password password) {
        Favorites favorites = favoritesService.findById(password.getFavoritesId());
        if (favorites != null) {
            Password save = passwordService.save(password);
            return ApiResponse.success(save);
        }
        return ApiResponse.error();
    }

    @GetMapping("/fid/{fid}")
    public ApiResponse queryByFavoritesId(@PathVariable Integer fid) {
        Password password = passwordService.findByFavoritesId(fid);
        if (password != null) {
            return ApiResponse.success(password);
        }
        return ApiResponse.error();
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        passwordService.deleteById(id);
        return ApiResponse.success();
    }
}
