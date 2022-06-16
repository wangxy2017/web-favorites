package com.wxy.web.favorites.controller.user;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.DataConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.model.SearchType;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.security.ContextUtils;
import com.wxy.web.favorites.security.SecurityUser;
import com.wxy.web.favorites.service.SearchTypeService;
import com.wxy.web.favorites.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

/**
 * @author wangxiaoyuan
 * 2021/1/5 11:25
 **/
@RestController
@RequestMapping("/search")
@Slf4j
@Api(tags = "搜索引擎")
@Secured("search")
public class SearchController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SearchTypeService searchTypeService;

    @Autowired
    private UserService userService;

    

    @GetMapping("/data")
    @ApiOperation(value = "查询搜索引擎")
    public ApiResponse data(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        SecurityUser user = ContextUtils.getCurrentUser();
        PageInfo<SearchType> page = searchTypeService.findPageByUserId(user.getId(), pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/system")
    @ApiOperation(value = "查询默认搜索引擎")
    public ApiResponse system() {
        return ApiResponse.success(DataConstants.SEARCH_LIST);
    }

    @PostMapping
    @ApiOperation(value = "新增搜索引擎")
    public ApiResponse save(@RequestBody SearchType searchType) {
        SecurityUser user = ContextUtils.getCurrentUser();
        searchType.setUserId(user.getId());
        String iconUrl;
        try {
            URL url = new URL(searchType.getUrl());
            iconUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + "/favicon.ico";
        } catch (Exception e) {
            iconUrl = PublicConstants.FAVORITES_ICON_DEFAULT;
        }
        searchType.setIcon(iconUrl);
        searchTypeService.save(searchType);
        return ApiResponse.success();
    }

    @GetMapping("/delete/{id}")
    @ApiOperation(value = "删除搜索引擎")
    public ApiResponse delete(@PathVariable Integer id) {
        searchTypeService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/query/{id}")
    @ApiOperation(value = "根据id查询")
    public ApiResponse query(@PathVariable Integer id) {
        SearchType searchType = searchTypeService.findById(id);
        return ApiResponse.success(searchType);
    }

}
