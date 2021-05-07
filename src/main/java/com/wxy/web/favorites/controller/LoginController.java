package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.*;
import com.wxy.web.favorites.service.*;
import com.wxy.web.favorites.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private SecretKeyService secretKeyService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private SpringUtils springUtils;

    @Autowired
    private AppConfig recommendsConfig;

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private VerificationService verificationService;

    @GetMapping("/email/code")
    public ApiResponse code(@RequestParam String email) {
        String code = RandomUtil.randomNumbers(PublicConstants.RANDOM_CODE_LENGTH);
        Date expTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(appConfig.getVerificationExpiredMinutes()));
        Verification verification = new Verification(null, email, code, expTime, PublicConstants.VERIFICATION_EMAIL_LOGIN);
        verificationService.save(verification);
        log.info("登录邮箱：{}，登录验证码：{}", email, code);
        emailUtils.sendSimpleMail(email, "网络收藏夹|登录", "您正在登录账号，验证码为：" + code + "，" + appConfig.getVerificationExpiredMinutes() + "分钟内有效。");
        return ApiResponse.success();
    }

    @PostMapping("/emailLogin")
    public ApiResponse emailLogin(@RequestParam String email, @RequestParam String code) {
        Verification verification = verificationService.findCode(email, PublicConstants.VERIFICATION_EMAIL_LOGIN);
        String loginEmailCode = verification != null && verification.getExpiredTime().getTime() > System.currentTimeMillis() ? verification.getCode() : null;
        if (StringUtils.isNotBlank(code) && code.equals(loginEmailCode)) {
            // 查询email是否注册，如果没有注册，先注册账号
            User user = userService.findByEmail(email);
            if (user == null) {
                // 注册用户
                String randomKey = RandomUtil.randomString(PublicConstants.USER_SECRET_KEY_LENGTH);
                String tempPwd = RandomUtil.randomString(PublicConstants.TEMP_PASSWORD_LENGTH);
                user = new User();
                user.setUsername(email);
                user.setPassword(DigestUtils.md5DigestAsHex((tempPwd + randomKey).getBytes()));
                user.setEmail(email);
                user.setCapacity(appConfig.getInitCapacity() * 1024 * 1024L);
                user = userService.save(user);
                // 保存secretKey
                SecretKey secretKey = new SecretKey(null, user.getId(), randomKey);
                secretKeyService.save(secretKey);
                // 创建默认分类
                Category category = new Category(null, PublicConstants.DEFAULT_CATEGORY_NAME, user.getId(), PublicConstants.SYSTEM_CATEGORY_CODE, PublicConstants.MAX_SORT_NUMBER, null, null);
                categoryService.save(category);
                // 推荐收藏
                Integer userId = user.getId();
                List<Favorites> favorites = recommendsConfig.getRecommends().stream().map(s -> {
                    String[] split = s.split(PublicConstants.DEFAULT_DELIMITER);
                    return new Favorites(null, split[0], split[1] + "favicon.ico"
                            , split[1], category.getId(), userId,
                            PinYinUtils.toPinyin(split[0]),
                            PinYinUtils.toPinyinS(split[0]),
                            null, null, null, null, null, null);
                }).collect(Collectors.toList());
                favoritesService.saveAll(favorites);
            }
            // 生成token
            String token = tokenUtils.createToken(user.getId(), TimeUnit.DAYS.toMillis(PublicConstants.REMEMBER_ME_DAYS));
            return ApiResponse.success(token);
        } else {
            return ApiResponse.error("验证码错误");
        }
    }

    @PostMapping
    public ApiResponse login(@RequestBody User user, @RequestParam(required = false) Integer remember) {
        User user1 = userService.findByUsername(user.getUsername());
        if (user1 != null) {
            SecretKey secretKey = secretKeyService.findByUserId(user1.getId());
            if (user1.getPassword().equals(DigestUtils.md5DigestAsHex((user.getPassword() + secretKey.getRandomKey()).getBytes()))) {
                String token;
                if (PublicConstants.REMEMBER_ME_CODE.equals(remember)) {
                    token = tokenUtils.createToken(user1.getId(), TimeUnit.DAYS.toMillis(14));
                } else {
                    token = tokenUtils.createToken(user1.getId());
                }
                return ApiResponse.success(token);
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
            String tempPwd = RandomUtil.randomString(PublicConstants.TEMP_PASSWORD_LENGTH);
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
}
