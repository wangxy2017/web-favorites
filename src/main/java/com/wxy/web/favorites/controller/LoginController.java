package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ApiResponse login(@RequestBody User user) {
        User user1 = userRepository.findByUsername(user.getUsername());
        if (user1 != null && user1.getPassword().equals(user.getPassword())) {
            SpringUtils.getRequest().getSession().setAttribute("user", user1);
            return ApiResponse.success();
        } else {
            return ApiResponse.error("用户名或密码错误");
        }
    }
}
