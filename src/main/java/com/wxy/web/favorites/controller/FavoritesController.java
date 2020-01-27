package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoritesController {

    @Autowired
    private FavoritesRepository favoritesRepository;

    @PostMapping("/save")
    public ApiResponse save(@RequestBody Favorites favorites) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        favorites.setUserId(user.getId());
        favoritesRepository.save(favorites);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse list() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        List<Favorites> list = favoritesRepository.findByUserId(user.getId());
        return ApiResponse.success(list);
    }

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        favoritesRepository.deleteById(id);
        return ApiResponse.success();
    }
}
