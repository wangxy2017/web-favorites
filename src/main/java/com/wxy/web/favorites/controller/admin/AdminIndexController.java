package com.wxy.web.favorites.controller.admin;

import com.wxy.web.favorites.constant.DataConstants;
import com.wxy.web.favorites.core.ApiResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author e-Xiaoyuan.Wang
 * @since 2022/6/15 14:07
 */
@RestController
@RequestMapping("/admin-index")
@Api(tags = "首页")
@Secured("ADMIN")
public class AdminIndexController {

    @Autowired
    private UserService userService;

    @Value("${admin.username}")
    private String adminUsername;

    @GetMapping("/info")
    @ApiOperation(value = "查询登录信息")
    public ApiResponse info() {
        SecurityUser securityUser = ContextUtils.getCurrentUser();
        User user = userService.findById(securityUser.getId());
        User user1 = JpaUtils.evictSession(user, User.class);
        user1.setPassword(null);
        user1.setPermissions(Objects.equals(user.getUsername(), adminUsername) ?
                DataConstants.SUPER_ADMIN_ROLE_LIST : DataConstants.ADMIN_ROLE_LIST);
        return ApiResponse.success(user1);
    }
}
