package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.PasswordUtils;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailUtils emailUtils;

    @PostMapping
    public ApiResponse login(@RequestBody User user) {
        User user1 = userRepository.findByUsername(user.getUsername());
        if (user1 != null && user1.getPassword().equals(user.getPassword())) {
            SpringUtils.getRequest().getSession().setAttribute("user", user1);
            return ApiResponse.success();
        } else {
            return ApiResponse.error("用户名或密码错误");
        }
    }

    @PostMapping("/forgot")
    public ApiResponse forgot(@RequestBody User user) {
        User u = userRepository.findByUsernameAndEmail(user.getUsername(), user.getEmail());
        if (u != null) {
            String tempPwd = PasswordUtils.randomPassword(8);
            // 重置用户密码
            u.setPassword(tempPwd);
            userRepository.save(u);
            // 将临时密码发送至用户邮箱
            emailUtils.send(u.getEmail(), "网络收藏夹|忘记密码",
                    String.format("密码已重置，为了您的账户安全，登录后请即时修改密码。临时密码：【%s】", tempPwd));
            return ApiResponse.success();
        } else {
            return ApiResponse.error("账号或邮箱不存在");
        }
    }
}
