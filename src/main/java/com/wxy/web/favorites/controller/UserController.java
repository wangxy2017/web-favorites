package com.wxy.web.favorites.controller;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.DataConstants;
import com.wxy.web.favorites.constant.EmailConstants;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.Verification;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.service.VerificationService;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.JpaUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
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
    private VerificationService verificationService;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username:}")
    private String mailTo;

    @GetMapping("/info")
    @ApiOperation(value = "查询登录信息")
    public ApiResponse info() {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
        User user1 = JpaUtils.evictSession(user, User.class);
        user1.setPassword(null);
        return ApiResponse.success(user1);
    }

    @GetMapping("/notice")
    @ApiOperation(value = "查询系统公告")
    public ApiResponse notice() {
        return ApiResponse.success(DataConstants.SYSTEM_NOTICE);
    }

    @PostMapping("/cleanData")
    @ApiOperation(value = "清除所有数据")
    public ApiResponse cleanData(@RequestParam String loginPwd) {
        SecurityUser user = ContextUtils.getCurrentUser();
        if (passwordEncoder.matches(loginPwd, user.getPassword())) {
            userService.deleteAllData(user.getId());
            return ApiResponse.success();
        } else {
            return ApiResponse.error(ErrorConstants.INVALID_PASSWORD_MSG);
        }
    }

    @PostMapping("/password")
    @ApiOperation(value = "重置密码")
    public ApiResponse password(@RequestParam String oldPassword, @RequestParam String newPassword) {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
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
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
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
        Verification verification = new Verification(null, email, code, expTime, PublicConstants.VERIFICATION_EMAIL_UPDATE, new Date());
        verificationService.save(verification);
        emailUtils.sendSimpleMail(email, EmailConstants.BINDING_EMAIL_TITLE, String.format(EmailConstants.BINDING_EMAIL_CONTENT, code, appConfig.getVerificationExpiredMinutes()));
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
                SecurityUser securityUser = ContextUtils.getCurrentUser();
                User user = userService.findById(securityUser.getId());
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
    public ApiResponse getUserData() {
        SecurityUser user = ContextUtils.getCurrentUser();
        Map<String, Object> userData = userService.findUserData(user.getId());
        return ApiResponse.success(userData);
    }

    @GetMapping("/online")
    @ApiOperation(value = "增加在线时长")
    public ApiResponse online() {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
        Date now = new Date();
        if (user.getRegisterTime() == null) {
            user.setRegisterTime(now);
            userService.save(user);
        }
        if (user.getOnlineHour() == null) {
            user.setLastOnlineTime(now);
            userService.save(user);
        }
        long between = DateUtil.between(user.getLastOnlineTime(), now, DateUnit.HOUR);
        if (between > 0) {
            user.setOnlineHour(Optional.ofNullable(user.getOnlineHour()).orElse(0) + 1);
            user.setLastOnlineTime(now);
            userService.save(user);
        }
        return ApiResponse.success();
    }

    @GetMapping("/search")
    @ApiOperation(value = "增加搜索次数")
    public ApiResponse search() {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
        user.setSearchCount(Optional.ofNullable(user.getSearchCount()).orElse(0) + 1);
        userService.save(user);
        return ApiResponse.success();
    }

    @GetMapping("/visit")
    @ApiOperation(value = "增加访问次数")
    public ApiResponse visit() {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
        user.setClickCount(Optional.ofNullable(user.getClickCount()).orElse(0) + 1);
        userService.save(user);
        return ApiResponse.success();
    }
}
