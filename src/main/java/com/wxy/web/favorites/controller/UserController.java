package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.EmailConstants;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.Verification;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.service.VerificationService;
import com.wxy.web.favorites.util.EmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api(tags = "用户")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private ContextUtils contextUtils;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/info")
    @ApiOperation(value = "查询登录信息")
    public ApiResponse info() {
        User user = contextUtils.getCurrentUser();
        user.setPassword(null);
        return ApiResponse.success(user);
    }

    @PostMapping("/password")
    @ApiOperation(value = "重置密码")
    public ApiResponse password(@RequestParam String oldPassword, @RequestParam String newPassword) {
        User user = contextUtils.getCurrentUser();
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);
            return ApiResponse.success();
        } else {
            return ApiResponse.error(ErrorConstants.INVALID_PASSWORD_MSG);
        }
    }

    @PostMapping("/style")
    @ApiOperation(value = "保存浏览模式")
    public ApiResponse viewStyle(@RequestParam Integer viewStyle) {
        User user = contextUtils.getCurrentUser();
        user.setViewStyle(viewStyle == 1 ? 1 : 0);
        userService.save(user);
        return ApiResponse.success();
    }

    @GetMapping("/email/code")
    @ApiOperation(value = "修改邮箱-获取验证码")
    public ApiResponse code(@RequestParam String email) {
        Assert.isTrue(verificationService.sendEnable(email, PublicConstants.VERIFICATION_EMAIL_UPDATE), "发送验证码太频繁");
        String code = RandomUtil.randomNumbers(PublicConstants.RANDOM_CODE_LENGTH);
        Date expTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(appConfig.getVerificationExpiredMinutes()));
        Verification verification = new Verification(null, email, code, expTime, PublicConstants.VERIFICATION_EMAIL_UPDATE,new Date());
        verificationService.save(verification);
        emailUtils.sendSimpleMail(email, EmailConstants.BINDING_EMAIL_TITLE, String.format(EmailConstants.BINDING_EMAIL_CONTENT,code,appConfig.getVerificationExpiredMinutes()));
        return ApiResponse.success();
    }

    @PostMapping("/email")
    @ApiOperation(value = "修改邮箱")
    public ApiResponse updateEmail(@RequestParam String newEmail, @RequestParam String code) {
        if (StrUtil.isNotBlank(newEmail) && StrUtil.isNotBlank(code)) {
            User user1 = userService.findByEmail(newEmail);
            Verification verification = verificationService.findCode(newEmail, PublicConstants.VERIFICATION_EMAIL_UPDATE);
            String emailCode = verification != null && verification.getExpiredTime().getTime() > System.currentTimeMillis() ? verification.getCode() : null;
            if (user1 == null && code.equals(emailCode)) {
                User user = contextUtils.getCurrentUser();
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
    @ApiOperation(value = "获取统计信息")
    public ApiResponse getUserData(){
        User user = contextUtils.getCurrentUser();
        Map<String,Object> userData = userService.findUserData(user.getId());
        return ApiResponse.success(userData);
    }
}
