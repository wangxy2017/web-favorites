package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.EmailConstants;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.Verification;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.service.VerificationService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private SpringUtils springUtils;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/info")
    public ApiResponse info() {
        User user = springUtils.getCurrentUser();
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PostMapping("/password")
    public ApiResponse password(@RequestParam String oldPassword, @RequestParam String newPassword) {
        User user = springUtils.getCurrentUser();
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);
            return ApiResponse.success();
        } else {
            return ApiResponse.error(ErrorConstants.INVALID_PASSWORD_MSG);
        }
    }

    @PostMapping("/style")
    public ApiResponse viewStyle(@RequestParam Integer viewStyle) {
        User user = springUtils.getCurrentUser();
        user.setViewStyle(viewStyle == 1 ? 1 : 0);
        userService.save(user);
        return ApiResponse.success();
    }

    @GetMapping("/email/code")
    public ApiResponse code(@RequestParam String email) {
        String code = RandomUtil.randomNumbers(PublicConstants.RANDOM_CODE_LENGTH);
        Date expTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(appConfig.getVerificationExpiredMinutes()));
        Verification verification = new Verification(null, email, code, expTime, PublicConstants.VERIFICATION_EMAIL_UPDATE);
        verificationService.save(verification);
        emailUtils.sendSimpleMail(email, EmailConstants.BINDING_EMAIL_TITLE, String.format(EmailConstants.BINDING_EMAIL_CONTENT,code,appConfig.getVerificationExpiredMinutes()));
        return ApiResponse.success();
    }

    @PostMapping("/email")
    public ApiResponse updateEmail(@RequestParam String newEmail, @RequestParam String code) {
        if (StringUtils.isNotBlank(newEmail) && StringUtils.isNotBlank(code)) {
            User user1 = userService.findByEmail(newEmail);
            Verification verification = verificationService.findCode(newEmail, PublicConstants.VERIFICATION_EMAIL_UPDATE);
            String emailCode = verification != null && verification.getExpiredTime().getTime() > System.currentTimeMillis() ? verification.getCode() : null;
            if (user1 == null && code.equals(emailCode)) {
                User user = springUtils.getCurrentUser();
                user.setEmail(newEmail);
                userService.save(user);
                // 移除验证码
                verificationService.deleteById(verification.getId());
                return ApiResponse.success();
            }
        }
        return ApiResponse.error();
    }

    @GetMapping("/data")
    public ApiResponse getUserData(){
        User user = springUtils.getCurrentUser();
        Map<String,Object> userData = userService.findUserData(user.getId());
        return ApiResponse.success(userData);
    }
}
