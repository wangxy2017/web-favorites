package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.dao.VerificationRepository;
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

@Slf4j
@RestController
@RequestMapping("/register")
public class RegisterController {

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

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping
    public ApiResponse register(@RequestBody User user) {
        if (userService.findByUsernameOrEmail(user.getUsername(), user.getEmail()) == null) {
            Verification verification = verificationService.findCode(user.getEmail(), 0);
            String code = verification != null && verification.getExpiredTime().getTime() > System.currentTimeMillis() ? verification.getCode() : null;
            if (StringUtils.isNotBlank(user.getCode()) && user.getCode().equals(code)) {
                String randomKey = RandomUtil.randomString(16);
                user.setPassword(DigestUtils.md5DigestAsHex((user.getPassword() + randomKey).getBytes()));
                user.setCapacity(appConfig.getInitCapacity() * 1024 * 1024L);
                User user1 = userService.save(user);
                // 保存secretKey
                SecretKey secretKey = new SecretKey(null, user1.getId(), randomKey);
                secretKeyService.save(secretKey);
                // 创建默认分类
                Category category = new Category(null, "默认分类", user1.getId(), 1, 9999, null, null);
                categoryService.save(category);
                // 推荐收藏
                List<Favorites> favorites = recommendsConfig.getRecommends().stream().map(s -> {
                    String[] split = s.split(",");
                    return new Favorites(null, split[0], split[1] + "favicon.ico"
                            , split[1], category.getId(), user1.getId(),
                            PinYinUtils.toPinyin(split[0]),
                            PinYinUtils.toPinyinS(split[0]),
                            null, null, null, null, null, null);
                }).collect(Collectors.toList());
                favoritesService.saveAll(favorites);
                // 生成token
                String token = tokenUtils.createToken(user1.getId());
                return ApiResponse.success(token);
            } else {
                return ApiResponse.error("验证码错误");
            }
        } else {
            return ApiResponse.error("用户名或邮箱已存在");
        }
    }

    @GetMapping("/{username}")
    public ApiResponse checkUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/email/code")
    public ApiResponse code(@RequestParam String email) {
        String code = RandomUtil.randomNumbers(6);
        Date expTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
        Verification verification = new Verification(null, email, code, expTime, 0);
        verificationService.save(verification);
        log.info("注册邮箱：{}，注册验证码：{}", email, code);
        emailUtils.sendSimpleMail(email, "网络收藏夹|注册", "您正在注册账号，验证码为：" + code + "，30分钟内有效。");
        return ApiResponse.success();
    }

    @GetMapping("/email/{email}")
    public ApiResponse checkEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }
}
