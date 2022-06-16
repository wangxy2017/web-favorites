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

import static com.wxy.web.favorites.constant.PublicConstants.NOTICE_CONFIG;

/**
 * <p>
 *
 * </p>
 *
 * @author e-Xiaoyuan.Wang
 * @since 2022/6/15 10:09
 */
@RestController
@RequestMapping("/system-notice")
@Api(tags = "公告设置")
@Secured("system_notice")
public class SystemNoticeController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/info")
    @ApiOperation(value = "查询公告")
    public ApiResponse info() {
        List<SystemConfig> list = systemConfigService.findByKeyCodeIn(NOTICE_CONFIG);
        Map<String, String> map = list.stream().collect(Collectors.toMap(SystemConfig::getKeyCode, SystemConfig::getKeyValue));
        return ApiResponse.success(map);
    }

    @PostMapping("/save")
    @ApiOperation(value = "保存公告")
    public ApiResponse save(@RequestBody Map<String, String> notice) {
        notice.forEach((key, val) -> {
            SystemConfig config = systemConfigService.findByKeyCode(key);
            if (config == null) {
                config = new SystemConfig();
                config.setKeyCode(key);
            }
            config.setKeyValue(val);
            systemConfigService.save(config);
        });
        return ApiResponse.success();
    }
}
