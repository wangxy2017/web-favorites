package com.wxy.web.favorites.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.QuickNavigation;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.service.QuickNavigationService;
import com.wxy.web.favorites.util.HtmlUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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

    @Autowired
    private AppConfig appConfig;

    @PostMapping
    @ApiOperation(value = "新增快捷导航")
    public ApiResponse save(@RequestBody QuickNavigation quickNavigation) {
        User user = contextUtils.getCurrentUser();
        List<QuickNavigation> list = quickNavigationService.findList(user.getId());
        Assert.isTrue(list == null || list.size() < appConfig.getNavigationLimit(), PublicConstants.NAVIGATION_LIMITED_MSG);
        quickNavigation.setUserId(user.getId());
        // 处理图标
        String icon = HtmlUtils.getIcon(quickNavigation.getUrl());
        quickNavigation.setIcon(StringUtils.isBlank(icon) ? PublicConstants.FAVORITES_ICON_DEFAULT : icon);
        quickNavigationService.save(quickNavigation);
        return ApiResponse.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询快捷导航")
    public ApiResponse findList() {
        Integer userId = contextUtils.getCurrentUser().getId();
        return ApiResponse.success(quickNavigationService.findList(userId));
    }

    @GetMapping("/delete/{id}")
    @ApiOperation(value = "删除快捷导航")
    public ApiResponse delete(@PathVariable Integer id) {
        quickNavigationService.deleteById(id);
        return ApiResponse.success();
    }
}
