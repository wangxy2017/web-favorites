package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    /**
     * 注册
     *
     * @param user
     * @return
     */
    @PostMapping
    public ApiResponse register(@RequestBody User user) {
        if (userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail()) == null) {
            User user1 = userRepository.save(user);
            // 创建默认分类
            Category category = new Category(null, "默认分类", user1.getId(), 1, null);
            categoryRepository.save(category);
            HttpSession session = SpringUtils.getRequest().getSession();
            session.setAttribute("user", user1);
            return ApiResponse.success();
        }
        return ApiResponse.error("用户名或邮箱已存在");
    }

    @GetMapping("/{username}")
    public ApiResponse checkUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/email/{email}")
    public ApiResponse checkEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }
}
