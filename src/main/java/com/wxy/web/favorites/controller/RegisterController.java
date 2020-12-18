package com.wxy.web.favorites.controller;

import cn.hutool.core.util.RandomUtil;
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
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/register")
public class RegisterController {

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

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping
    public ApiResponse register(@RequestBody User user) {
        if (userService.findByUsernameOrEmail(user.getUsername(), user.getEmail()) == null) {
            HttpSession session = SpringUtils.getRequest().getSession();
            String code = (String) session.getAttribute("register_email_code");
            if (StringUtils.isNotBlank(user.getCode()) && user.getCode().equals(code)) {
                String randomKey = RandomUtil.randomString(16);
                user.setPassword(DigestUtils.md5DigestAsHex((user.getPassword() + randomKey).getBytes()));
                User user1 = userService.save(user);
                // 保存secretKey
                SecretKey secretKey = new SecretKey(null, user1.getId(), randomKey);
                secretKeyService.save(secretKey);
                // 创建默认分类
                Category category = new Category(null, "默认分类", user1.getId(), 1, 9999, null, null);
                categoryService.save(category);
                // 推荐收藏
                long s = System.currentTimeMillis();
                Map<String, String> recommendMap = new HashMap<>();
                recommendMap.put("bilibili", "https://www.bilibili.com/");
                recommendMap.put("淘宝", "https://www.taobao.com/");
                recommendMap.put("知乎", "https://www.zhihu.com/");
                recommendMap.put("京东", "https://www.jd.com/");
                recommendMap.put("腾讯视频", "https://v.qq.com/");
                recommendMap.put("今日头条", "https://www.toutiao.com/");
                recommendMap.forEach((k, v) -> favoritesService.save(new Favorites(null, k, v + "favicon.ico"
                        , v, category.getId(), user1.getId(),
                        PinYinUtils.toPinyin(k),
                        PinYinUtils.toPinyinS(k), null, null, null, null, null)));
                if (log.isDebugEnabled())
                    log.debug("插入推荐数据耗时：{}ms", System.currentTimeMillis() - s);
                // 设置session
                session.setAttribute("login_user", user1);
                // 移除验证码
                session.removeAttribute("register_email_code");
                return ApiResponse.success();
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
        log.info("注册验证码：{}", code);
        HttpSession session = SpringUtils.getRequest().getSession();
        session.setAttribute("register_email_code", code);
        emailUtils.send(email, "网络收藏夹|注册", "您正在注册账号，验证码为：" + code + "，30分钟内有效。");
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
