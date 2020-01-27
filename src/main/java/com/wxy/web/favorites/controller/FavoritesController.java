package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.util.ApiResponse;
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
        favoritesRepository.save(favorites);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    public ApiResponse list() {
        List<Favorites> list = favoritesRepository.findAll();
        return ApiResponse.success(list);
    }
}
