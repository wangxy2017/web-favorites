package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.PasswordUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailUtils emailUtils;

    @PostMapping
    public ApiResponse login(@RequestBody User user, @RequestParam String remember, HttpServletResponse response) {
        User user1 = userRepository.findByUsername(user.getUsername());
        if (user1 != null && user1.getPassword().equals(user.getPassword())) {
            HttpServletRequest request = SpringUtils.getRequest();
            request.getSession().setAttribute("user", user1);
            if ("1".equals(remember)) {
                Cookie username = new Cookie("username", user1.getUsername());
                username.setPath("/");
                username.setMaxAge(60 * 60 * 24 * 14);
                response.addCookie(username);
                Cookie password = new Cookie("password", user1.getPassword());
                password.setPath("/");
                password.setMaxAge(60 * 60 * 24 * 14);
                response.addCookie(password);
            }
            return ApiResponse.success();
        } else {
            return ApiResponse.error("用户名或密码错误");
        }
    }

    @PostMapping("/forgot")
    public ApiResponse forgot(@RequestBody User user) {
        User u = userRepository.findByUsernameAndEmail(user.getUsername(), user.getEmail());
        if (u != null) {
            String tempPwd = PasswordUtils.randomPassword(8);
            // 重置用户密码
            u.setPassword(tempPwd);
            userRepository.save(u);
            // 将临时密码发送至用户邮箱
            emailUtils.send(u.getEmail(), "网络收藏夹|重置密码",
                    String.format("您的新密码：%s，请牢记。", tempPwd));
            return ApiResponse.success();
        } else {
            return ApiResponse.error("账号或邮箱不存在");
        }
    }
}
