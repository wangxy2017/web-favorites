package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailUtils emailUtils;

    @GetMapping("/logout")
    public ApiResponse logout() {
        HttpServletRequest request = SpringUtils.getRequest();
        request.getSession().removeAttribute("user");
        // 清除cookie
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                Cookie cookie = new Cookie(c.getName(), null);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                SpringUtils.getResponse().addCookie(cookie);
            }
        }
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
            // 发送邮件
            emailUtils.send(user.getEmail(), "网络收藏夹|修改密码",
                    String.format("您的新密码：%s，请牢记。", newPassword));
            return ApiResponse.success();
        } else {
            return ApiResponse.error("身份验证错误");
        }
    }
}
