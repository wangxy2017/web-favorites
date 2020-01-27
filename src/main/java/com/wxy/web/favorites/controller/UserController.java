package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/password")
    public ApiResponse password(String oldPassword, String newPassword) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        if (user.getPassword().equals(oldPassword)) {
            user.setPassword(newPassword);
            userRepository.save(user);
            return ApiResponse.success();
        } else {
            return ApiResponse.error("身份验证错误");
        }
    }
}
