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
        User user = SpringUtils.getCurrentUser();
        return ApiResponse.success(user);
    }

    @PostMapping("/password")
    public ApiResponse password(@RequestParam String oldPassword, @RequestParam String newPassword) {
        User user = SpringUtils.getCurrentUser();
        SecretKey secretKey = secretKeyService.findByUserId(user.getId());
        if (user.getPassword().equals(DigestUtils.md5DigestAsHex((oldPassword + secretKey.getRandomKey()).getBytes()))) {
            user.setPassword(DigestUtils.md5DigestAsHex((newPassword + secretKey.getRandomKey()).getBytes()));
            userService.save(user);
            return ApiResponse.success();
        } else {
            return ApiResponse.error("身份验证错误");
        }
    }

    @PostMapping("/style")
    public ApiResponse viewStyle(@RequestParam Integer viewStyle) {
        User user = SpringUtils.getCurrentUser();
        user.setViewStyle(viewStyle == 1 ? 1 : 0);
        userService.save(user);
        return ApiResponse.success();
    }

    @GetMapping("/email/code")
    public ApiResponse code(@RequestParam String email) {
        String code = RandomUtil.randomNumbers(6);
        HttpSession session = SpringUtils.getRequest().getSession();
        session.setAttribute("email_update_code", code);
        emailUtils.send(email, "网络收藏夹|绑定邮箱", "您正在绑定邮箱，验证码为：" + code + "，30分钟内有效。");
        return ApiResponse.success();
    }

    @PostMapping("/email")
    public ApiResponse updateEmail(@RequestParam String newEmail, @RequestParam String code) {
        if (StringUtils.isNotBlank(newEmail) && StringUtils.isNotBlank(code)) {
            HttpSession session = SpringUtils.getRequest().getSession();
            String emailCode = (String) session.getAttribute("email_update_code");
            User user1 = userService.findByEmail(newEmail);
            if (user1 == null && code.equals(emailCode)) {
                User user = SpringUtils.getCurrentUser();
                user.setEmail(newEmail);
                userService.save(user);
                session.removeAttribute("email_update_code");
                return ApiResponse.success();
            }
        }
        return ApiResponse.error();
    }
}
