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
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PinYinUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping
    public ApiResponse register(@RequestBody User user) {
        String password = user.getPassword();
        if (userService.findByUsernameOrEmail(user.getUsername(), user.getEmail()) == null) {
            SecretKey secretKey = new SecretKey(null, user.getUsername(), RandomUtil.randomString(16));
            secretKeyService.save(secretKey);
            user.setPassword(DigestUtils.md5DigestAsHex((user.getPassword() + secretKey.getRandomKey()).getBytes()));
            User user1 = userService.save(user);
            // 创建默认分类
            Category category = new Category(null, "默认分类", user1.getId(), 1, 9999, null);
            categoryService.save(category);
            // 推荐收藏
            List<Favorites> recommends = new ArrayList<>();
            recommends.add(new Favorites(null, "百度搜索", "https://www.baidu.com/favicon.ico", "https://www.baidu.com/", category.getId(), user1.getId(), PinYinUtils.toPinyin("百度搜索"), null, null, null));
            recommends.add(new Favorites(null, "谷歌翻译", "https://translate.google.cn/favicon.ico", "https://translate.google.cn/", category.getId(), user1.getId(), PinYinUtils.toPinyin("谷歌翻译"), null, null, null));
            favoritesService.saveAll(recommends);
            // 设置session
            SpringUtils.getRequest().getSession().setAttribute("user", user1);
            return ApiResponse.success();
        }
        return ApiResponse.error("用户名或邮箱已存在");
    }

    @GetMapping("/{username}")
    public ApiResponse checkUsername(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
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
