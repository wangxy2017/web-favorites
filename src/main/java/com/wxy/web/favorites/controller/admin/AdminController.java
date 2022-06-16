package com.wxy.web.favorites.controller.admin;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.constant.DataConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.JpaUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@Api(tags = "账号管理")
@Secured("admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Value("${admin.username}")
    private String adminUsername;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/disable/{id}")
    @ApiOperation(value = "启用禁用")
    public ApiResponse disable(@PathVariable Integer id) {
        User user = userService.findById(id);
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(!Objects.equals(user.getUsername(), adminUsername), "超级管理员不可禁用");
        user.setStatus(Objects.equals(user.getStatus(), 2) ? 1 : 2);
        userService.save(user);
        return ApiResponse.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除")
    public ApiResponse delete(@PathVariable Integer id) {
        User user = userService.findById(id);
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(Objects.equals(user.getAdmin(), 1), "注册用户不可删除");
        Assert.isTrue(!Objects.equals(user.getUsername(), adminUsername), "超级管理员不可删除");
        userService.deleteById(id);
        return ApiResponse.success();
    }

    @PostMapping("/save")
    @ApiOperation(value = "新增")
    public ApiResponse save(@RequestBody User user) {
        Assert.isNull(userService.findByUsername(user.getUsername()), "账号已存在");
        user.setPassword(passwordEncoder.encode(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8))));
        user.setAdmin(1);
        user.setRegisterTime(new Date());
        userService.save(user);
        return ApiResponse.success();
    }

    @PostMapping("/updatePwd")
    @ApiOperation(value = "修改密码")
    public ApiResponse updatePwd(@RequestBody User user) {
        User user1 = userService.findById(user.getId());
        Assert.notNull(user1, "账号不存在");
        Assert.isTrue(!Objects.equals(user1.getUsername(), adminUsername), "超级管理员不可修改密码");
        user1.setPassword(passwordEncoder.encode(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8))));
        userService.save(user1);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取管理员列表")
    public ApiResponse list(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer pageNum,
                            @RequestParam(required = false) Integer pageSize) {
        PageInfo<User> page = userService.findAdminPageList(name, pageNum, pageSize);
        List<User> list = page.getList().stream().map(user -> {
            User user1 = JpaUtils.evictSession(user, User.class);
            user1.setPassword(null);
            return user1;
        }).collect(Collectors.toList());
        page.setList(list);
        return ApiResponse.success(page);
    }
}
