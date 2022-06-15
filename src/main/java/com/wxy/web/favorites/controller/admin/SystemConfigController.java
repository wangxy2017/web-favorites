package com.wxy.web.favorites.controller.admin;

import cn.hutool.core.lang.Assert;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.model.SystemConfig;
import com.wxy.web.favorites.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *
 * </p>
 *
 * @author e-Xiaoyuan.Wang
 * @since 2022/6/15 10:09
 */
@RestController
@RequestMapping("/system-config")
@Api(tags = "系统配置")
@Secured("ADMIN")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/list")
    @ApiOperation(value = "查询配置列表")
    public ApiResponse list() {
        return ApiResponse.success(systemConfigService.findAll());
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改配置")
    public ApiResponse update(@RequestParam String keyCode, @RequestParam String keyValue) {
        SystemConfig config = systemConfigService.findByKeyCode(keyCode);
        Assert.notNull(config, "配置不存在");
        config.setKeyValue(keyValue);
        systemConfigService.save(config);
        return ApiResponse.success();
    }
}
