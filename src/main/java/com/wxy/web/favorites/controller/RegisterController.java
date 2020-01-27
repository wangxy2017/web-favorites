package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserRepository userRepository;


    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping
    public ApiResponse register(@RequestBody User user) {
        User user1 = userRepository.save(user);
        HttpSession session = SpringUtils.getRequest().getSession();
        session.setAttribute("user", user1);
        return ApiResponse.success();
    }
}
