package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
import com.wxy.web.favorites.config.RecommendsConfig;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.SecretKey;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.CategoryService;
import com.wxy.web.favorites.service.FavoritesService;
import com.wxy.web.favorites.service.SecretKeyService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Value("${app.init-capacity:100}")
    private long initCapacity;

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
    private RecommendsConfig recommendsConfig;

    @Value("${aes-key:B!D&LL5lyk62lnHi}")
    private String aesKey;

    @GetMapping("/email/code")
    public ApiResponse code(@RequestParam String email) {
        String code = RandomUtil.randomNumbers(6);
        log.info("登录邮箱：{}，登录验证码：{}", email, code);
        HttpSession session = springUtils.getRequest().getSession();
        session.setAttribute("login_email_code", code);
        emailUtils.sendSimpleMail(email, "网络收藏夹|登录", "您正在登录账号，验证码为：" + code + "，30分钟内有效。");
        return ApiResponse.success();
    }

    @PostMapping("/emailLogin")
    public ApiResponse emailLogin(@RequestParam String email, @RequestParam String code) {
        HttpSession session = springUtils.getRequest().getSession();
        String loginEmailCode = (String) session.getAttribute("login_email_code");
        if (StringUtils.isNotBlank(code) && code.equals(loginEmailCode)) {
            // 查询email是否注册，如果没有注册，先注册账号
            User user = userService.findByEmail(email);
            if (user == null) {
                // 注册用户
                String randomKey = RandomUtil.randomString(16);
                String tempPwd = RandomUtil.randomString(8);
                user = new User();
                user.setUsername(email);
                user.setPassword(DigestUtils.md5DigestAsHex((tempPwd + randomKey).getBytes()));
                user.setEmail(email);
                user.setCapacity(initCapacity * 1024 * 1024L);
                user = userService.save(user);
                // 保存secretKey
                SecretKey secretKey = new SecretKey(null, user.getId(), randomKey);
                secretKeyService.save(secretKey);
                // 创建默认分类
                Category category = new Category(null, "默认分类", user.getId(), 1, 9999, null, null);
                categoryService.save(category);
                // 推荐收藏
                Integer userId = user.getId();
                List<Favorites> favorites = recommendsConfig.getRecommends().stream().map(s -> {
                    String[] split = s.split(",");
                    return new Favorites(null, split[0], split[1] + "favicon.ico"
                            , split[1], category.getId(), userId,
                            PinYinUtils.toPinyin(split[0]),
                            PinYinUtils.toPinyinS(split[0]),
                            null, null, null, null, null, null);
                }).collect(Collectors.toList());
                favoritesService.saveAll(favorites);
            }
            // 设置session
            session.setAttribute("login_user", user);
            // 移除验证码
            session.removeAttribute("login_email_code");
            // 存入cookie
            String tokenValue = null;
            try {
                tokenValue = AESUtils.encrypt(user.getUsername() + "&&" + user.getPassword(), aesKey);
            } catch (Exception e) {
                log.error("token加密失败！！！", e);
            }
            if (tokenValue != null) {
                Cookie token = new Cookie("token", tokenValue);
                token.setPath("/");
                token.setMaxAge(60 * 60 * 24 * 14);
                springUtils.getResponse().addCookie(token);
            }
            return ApiResponse.success();
        } else {
            return ApiResponse.error("验证码错误");
        }
    }

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
