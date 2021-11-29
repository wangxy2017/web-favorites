package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.model.SearchType;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.SearchTypeService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.security.ContextUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author wangxiaoyuan
 * 2021/1/5 11:25
 **/
@RestController
@RequestMapping("/search")
@Slf4j
@Api(tags = "搜索引擎管理")
public class SearchController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SearchTypeService searchTypeService;

    @Autowired
    private ContextUtils contextUtils;

    @GetMapping("/data")
    @ApiOperation(value = "查询默认搜索引擎")
    public ApiResponse data(@RequestParam Integer pageNum,@RequestParam Integer pageSize) {
        User user = contextUtils.getCurrentUser();
        PageInfo<SearchType> page = searchTypeService.findPageByUserId(user.getId(),pageNum,pageSize);
        return ApiResponse.success(page);
    }

    @PostMapping
    @ApiOperation(value = "新增搜索引擎")
    public ApiResponse save(@RequestBody SearchType searchType) {
        User user = contextUtils.getCurrentUser();
        searchType.setUserId(user.getId());
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
