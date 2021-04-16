package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.model.SecretKey;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.SecretKeyService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.AESUtils;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecretKeyService secretKeyService;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private SpringUtils springUtils;

    @Value("${aes-key}")
    private String aesKey;

    @PostMapping
    public ApiResponse login(@RequestBody User user, @RequestParam(required = false) String remember) {
        User user1 = userService.findByUsername(user.getUsername());
        if (user1 != null) {
            SecretKey secretKey = secretKeyService.findByUserId(user1.getId());
            if (user1.getPassword().equals(DigestUtils.md5DigestAsHex((user.getPassword() + secretKey.getRandomKey()).getBytes()))) {
                HttpServletRequest request = springUtils.getRequest();
                request.getSession().setAttribute("login_user", user1);
                if ("1".equals(remember)) {
                    String tokenValue = null;
                    try {
                        tokenValue = AESUtils.encrypt(user1.getUsername() + "&&" + user1.getPassword(), aesKey);
                    } catch (Exception e) {
                        log.error("token加密失败！！！", e);
                    }
                    if (tokenValue != null) {
                        Cookie token = new Cookie("token", tokenValue);
                        token.setPath("/");
                        token.setMaxAge(60 * 60 * 24 * 14);
                        springUtils.getResponse().addCookie(token);
                    }
                }
                return ApiResponse.success();
            } else {
                return ApiResponse.error("用户名或密码错误");
            }
        } else {
            return ApiResponse.error("请先注册账号");
        }
    }

    @PostMapping("/forgot")
    public ApiResponse forgot(@RequestBody User user) {
        User user1 = userService.findByUsernameAndEmail(user.getUsername(), user.getEmail());
        if (user1 != null) {
            String tempPwd = RandomUtil.randomString(8);
            SecretKey secretKey = secretKeyService.findByUserId(user1.getId());
            // 重置用户密码
            user1.setPassword(DigestUtils.md5DigestAsHex((tempPwd + secretKey.getRandomKey()).getBytes()));
            userService.save(user1);
            // 将临时密码发送至用户邮箱
            emailUtils.sendSimpleMail(user1.getEmail(), "网络收藏夹|重置密码",
                    String.format("您的新密码：%s，请牢记。", tempPwd));
            return ApiResponse.success();
        } else {
            return ApiResponse.error("账号或邮箱不存在");
        }
    }

    @GetMapping("/out")
    public ApiResponse logout() {
        HttpServletRequest request = springUtils.getRequest();
        request.getSession().removeAttribute("login_user");
        // 清除cookie
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                Cookie cookie = new Cookie(c.getName(), null);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                springUtils.getResponse().addCookie(cookie);
            }
        }
        return ApiResponse.success();
    }
}
