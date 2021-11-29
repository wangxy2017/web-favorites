package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.QuickNavigation;
import com.wxy.web.favorites.service.QuickNavigationService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.security.ContextUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年11月25日
 */
@RestController
@RequestMapping("/quick-navigation")
@Slf4j
@Api(tags = "快捷导航")
public class QuickNavigationController {

    @Autowired
    private QuickNavigationService quickNavigationService;

    @Autowired
    private ContextUtils contextUtils;

    @PostMapping
    @ApiOperation(value = "新增快捷导航")
    public ApiResponse save(@RequestBody QuickNavigation quickNavigation) {
        quickNavigation.setUserId(contextUtils.getCurrentUser().getId());
        return ApiResponse.success(quickNavigationService.save(quickNavigation));
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询快捷导航")
    public ApiResponse findList() {
        Integer userId = contextUtils.getCurrentUser().getId();
        return ApiResponse.success(quickNavigationService.findList(userId));
    }
}
