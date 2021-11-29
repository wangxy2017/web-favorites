package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.EmailConstants;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.Verification;
import com.wxy.web.favorites.security.TokenUtil;
import com.wxy.web.favorites.service.CategoryService;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.service.VerificationService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.PinYinUtils;
import com.wxy.web.favorites.util.SpringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/register")
@Api(tags = "注册")
public class RegisterController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private UserService userService;

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
    private VerificationService verificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping
    @ApiOperation(value = "用户注册")
    public ApiResponse register(@RequestBody User user) {
        if (userService.findByUsernameOrEmail(user.getUsername(), user.getEmail()) == null) {
            Verification verification = verificationService.findCode(user.getEmail(), PublicConstants.VERIFICATION_REGISTER);
            String code = verification != null && verification.getExpiredTime().getTime() > System.currentTimeMillis() ? verification.getCode() : null;
            if (StringUtils.isNotBlank(user.getCode()) && user.getCode().equals(code)) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setCapacity(appConfig.getInitCapacity() * 1024 * 1024L);
                User user1 = userService.save(user);
                // 创建默认分类
                Category category = new Category(null, PublicConstants.DEFAULT_CATEGORY_NAME, user1.getId(), PublicConstants.SYSTEM_CATEGORY_CODE, PublicConstants.MAX_SORT_NUMBER, null, null,null);
                categoryService.save(category);
                // 推荐收藏
                List<Favorites> favorites = recommendsConfig.getRecommends().stream().map(s -> {
                    String[] split = s.split(PublicConstants.DEFAULT_DELIMITER);
                    return new Favorites(null, split[0], split[1] + "favicon.ico"
                            , split[1], category.getId(), user1.getId(),
                            PinYinUtils.toPinyin(split[0]),
                            PinYinUtils.toPinyinS(split[0]),
                            null, null, null, null, null, null, null, null,null,null,null);
                }).collect(Collectors.toList());
                favoritesService.saveAll(favorites);
                // 生成token
                String token = tokenUtil.generateToken(user1.getUsername());
                // 移除验证码
                verificationService.deleteById(verification.getId());
                return ApiResponse.success(token);
            } else {
                return ApiResponse.error(ErrorConstants.INVALID_VERIFICATION_MSG);
            }
        } else {
            return ApiResponse.error(ErrorConstants.USERNAME_OR_EMAIL_EXISTED_MSG);
        }
    }

    @GetMapping("/{username}")
    @ApiOperation(value = "查询用户名是否存在")
    public ApiResponse checkUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/email/code")
    @ApiOperation(value = "邮箱注册-获取验证码")
    public ApiResponse code(@RequestParam String email) {
        Assert.isTrue(verificationService.sendEnable(email, PublicConstants.VERIFICATION_REGISTER), "发送验证码太频繁");
        String code = RandomUtil.randomNumbers(PublicConstants.RANDOM_CODE_LENGTH);
        Date expTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(appConfig.getVerificationExpiredMinutes()));
        Verification verification = new Verification(null, email, code, expTime, PublicConstants.VERIFICATION_REGISTER,new Date());
        verificationService.save(verification);
        log.info("注册邮箱：{}，注册验证码：{}", email, code);
        emailUtils.sendSimpleMail(email, EmailConstants.REGISTER_TITLE, String.format(EmailConstants.REGISTER_CONTENT, code, appConfig.getVerificationExpiredMinutes()));
        return ApiResponse.success();
    }

    @GetMapping("/email/{email}")
    @ApiOperation(value = "验证邮箱是否存在")
    public ApiResponse checkEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }
}
