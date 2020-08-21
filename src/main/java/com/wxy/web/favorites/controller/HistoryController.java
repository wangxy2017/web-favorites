package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.History;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.HistoryService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @PostMapping
    public ApiResponse save(@RequestBody History history) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        history.setUserId(user.getId());
        historyService.save(history);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse findAll() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        List<Favorites> list = historyService.findHistoryFavorites(user.getId());
        return ApiResponse.success(list);
    }

    @DeleteMapping("/all")
    public ApiResponse deleteAll() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        historyService.deleteByUserId(user.getId());
        return ApiResponse.success();
    }
}
