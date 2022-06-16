package com.wxy.web.favorites.controller.admin;

import cn.hutool.core.lang.Assert;
import com.wxy.web.favorites.constant.EmailConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.EmailUtils;
import com.wxy.web.favorites.util.JpaUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin-user")
@Api(tags = "用户管理")
@Secured("admin_user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtils emailUtils;

    @GetMapping("/enable/{id}")
    @ApiOperation(value = "启用禁用")
    public ApiResponse enable(@PathVariable Integer id) {
        User user = userService.findById(id);
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(!Objects.equals(user.getAdmin(), 1), "用户角色不正确");
        user.setStatus(Objects.equals(user.getStatus(), 2) ? 1 : 2);
        userService.save(user);
        return ApiResponse.success();
    }

    @GetMapping("/clean/{id}")
    @ApiOperation(value = "清空用户数据")
    public ApiResponse clean(@PathVariable Integer id) {
        User user = userService.findById(id);
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(!Objects.equals(user.getAdmin(), 1), "用户角色不正确");
        userService.deleteAllData(id);
        return ApiResponse.success();
    }

    @PostMapping("/sendMail/")
    @ApiOperation(value = "发送邮件")
    public ApiResponse sendMail(@RequestParam Integer id, @RequestParam String content) {
        User user = userService.findById(id);
        Assert.notNull(user, "用户不存在");
        Assert.isTrue(!Objects.equals(user.getAdmin(), 1), "用户角色不正确");
        emailUtils.sendHtmlMail(user.getEmail(), EmailConstants.ADMIN_NOTICE_TITLE, content);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取用户列表")
    public ApiResponse userList(@RequestParam(required = false) String name,
                                @RequestParam(required = false) Integer pageNum,
                                @RequestParam(required = false) Integer pageSize) {
        PageInfo<User> page = userService.findUserPageList(name, pageNum, pageSize);
        List<User> list = page.getList().stream().map(user -> {
            User user1 = JpaUtils.evictSession(user, User.class);
            user1.setPassword(null);
            return user1;
        }).collect(Collectors.toList());
        page.setList(list);
        return ApiResponse.success(page);
    }
}
