package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.model.SecretKey;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.SecretKeyService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecretKeyService secretKeyService;

    @Autowired
    private EmailUtils emailUtils;

    @GetMapping("/info")
    public ApiResponse info() {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        return ApiResponse.success(user);
    }

    @PostMapping("/password")
    public ApiResponse password(String oldPassword, String newPassword) {
        User user = (User) SpringUtils.getRequest().getSession().getAttribute("user");
        SecretKey secretKey = secretKeyService.findByUsername(user.getUsername());
        if (user.getPassword().equals(DigestUtils.md5DigestAsHex((oldPassword + secretKey.getRandomKey()).getBytes()))) {
            user.setPassword(DigestUtils.md5DigestAsHex((newPassword + secretKey.getRandomKey()).getBytes()));
            userService.save(user);
            return ApiResponse.success();
        } else {
            return ApiResponse.error("身份验证错误");
        }
    }

    @GetMapping("/email/code")
    public ApiResponse code(@RequestParam String email) {
        String code = RandomUtil.randomNumbers(6);
        HttpSession session = SpringUtils.getRequest().getSession();
        session.setAttribute("code", code);
        session.setMaxInactiveInterval(15 * 60);
        emailUtils.send(email, "网络收藏夹|修改邮箱", "您正在修改邮箱，验证码为：" + code + "，15分钟内有效。");
        return ApiResponse.success();
    }

    @PostMapping("/email")
    public ApiResponse updateEmail(@RequestParam String email, @RequestParam String code) {
        HttpSession session = SpringUtils.getRequest().getSession();
        String code1 = (String) session.getAttribute("code");
        User user = (User) session.getAttribute("user");
        User user1 = userService.findByEmail(email);
        if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(code) && user1 == null && code1.equals(code)) {
            user.setEmail(email);
            userService.save(user);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }
}
