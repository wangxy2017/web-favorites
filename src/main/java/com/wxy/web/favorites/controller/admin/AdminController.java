package com.wxy.web.favorites.controller.admin;

import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.JpaUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Api(tags = "管理员")
@Secured("ADMIN")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    @ApiOperation(value = "查询登录信息")
    public ApiResponse info() {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
        User user1 = JpaUtils.evictSession(user, User.class);
        user1.setPassword(null);
        return ApiResponse.success(user1);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取管理员列表")
    public ApiResponse list(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer pageNum,
                            @RequestParam(required = false) Integer pageSize) {
        PageInfo<User> page = userService.findAdminPageList(name, pageNum, pageSize);
        return ApiResponse.success(page);
    }
}
