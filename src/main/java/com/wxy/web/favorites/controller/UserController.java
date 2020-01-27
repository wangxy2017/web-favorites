package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/logout")
    public ApiResponse logout() {
        SpringUtils.getRequest().getSession().removeAttribute("user");
        return ApiResponse.success();
    }

    @GetMapping("/info")
    public ApiResponse info() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        return ApiResponse.success(user);
    }
}
