package com.wxy.web.favorites.controller.admin;

import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.model.SystemConfig;
import com.wxy.web.favorites.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@Secured("system_config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/info")
    @ApiOperation(value = "查询配置")
    public ApiResponse info() {
        List<SystemConfig> list = systemConfigService.findAll();
        Map<String, String> map = list.stream().collect(Collectors.toMap(SystemConfig::getKeyCode, SystemConfig::getKeyValue));
        return ApiResponse.success(map);
    }

    @PostMapping("/save")
    @ApiOperation(value = "保存配置")
    public ApiResponse save(@RequestBody Map<String, String> config) {
        config.forEach((key, val) -> {
            SystemConfig config1 = systemConfigService.findByKeyCode(key);
            if (config1 == null) {
                config1 = new SystemConfig();
                config1.setKeyCode(key);
            }
            config1.setKeyValue(val);
            systemConfigService.save(config1);
        });
        return ApiResponse.success();
    }
}
