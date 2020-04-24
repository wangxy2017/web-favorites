package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PasswordUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public ApiResponse info() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        return ApiResponse.success(user);
    }

    @PostMapping("/password")
    public ApiResponse password(String oldPassword, String newPassword) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        if (user.getPassword().equals(DigestUtils.md5DigestAsHex((oldPassword + user.getRandomKey()).getBytes()))) {
            user.setRandomKey(PasswordUtils.randomPassword(10));
            user.setPassword(DigestUtils.md5DigestAsHex((newPassword + user.getRandomKey()).getBytes()));
            userService.save(user);
            return ApiResponse.success();
        } else {
            return ApiResponse.error("身份验证错误");
        }
    }
}
