package com.wxy.web.favorites.controller.admin;

import com.wxy.web.favorites.service.SystemConfigService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
