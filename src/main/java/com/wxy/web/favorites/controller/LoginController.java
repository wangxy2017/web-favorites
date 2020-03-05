package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.PasswordUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

@RestController
@RequestMapping("/login")
public class LoginController {

    private Base64.Encoder encoder = Base64.getEncoder();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailUtils emailUtils;

    @PostMapping
    public ApiResponse login(@RequestBody User user, @RequestParam(required = false) String remember) {
        User user1 = userRepository.findByUsername(user.getUsername());
        if (user1 != null && user1.getPassword().equals(DigestUtils.md5DigestAsHex((user.getPassword() + user1.getRandomKey()).getBytes()))) {
            HttpServletRequest request = SpringUtils.getRequest();
            request.getSession().setAttribute("user", user1);
            if ("1".equals(remember)) {
                Cookie token = new Cookie("token", encoder.encodeToString((user1.getUsername() + "&&" + user1.getPassword()).getBytes()));
                token.setPath("/");
                token.setMaxAge(60 * 60 * 24 * 14);
                SpringUtils.getResponse().addCookie(token);
            }
            return ApiResponse.success();
        } else {
            return ApiResponse.error("用户名或密码错误");
        }
    }

    @PostMapping("/forgot")
    public ApiResponse forgot(@RequestBody User user) {
        User user1 = userRepository.findByUsernameAndEmail(user.getUsername(), user.getEmail());
        if (user1 != null) {
            String tempPwd = PasswordUtils.randomPassword(8);
            // 重置用户密码
            user.setRandomKey(PasswordUtils.randomPassword(10));
            user.setPassword(DigestUtils.md5DigestAsHex((tempPwd + user.getRandomKey()).getBytes()));
            userRepository.save(user1);
            // 将临时密码发送至用户邮箱
            emailUtils.send(user1.getEmail(), "网络收藏夹|重置密码",
                    String.format("您的新密码：%s，请牢记。", tempPwd));
            return ApiResponse.success();
        } else {
            return ApiResponse.error("账号或邮箱不存在");
        }
    }

    @GetMapping("/out")
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
}
