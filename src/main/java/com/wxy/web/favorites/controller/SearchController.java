package com.wxy.web.favorites.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.model.SearchType;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.SearchTypeService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PageInfo;
import com.wxy.web.favorites.util.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangxiaoyuan
 * 2021/1/5 11:25
 **/
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private SearchTypeService searchTypeService;

    @Autowired
    private SpringUtils springUtils;

    @GetMapping("/data")
    public ApiResponse data(@RequestParam Integer pageNum,@RequestParam Integer pageSize) {
        User user = springUtils.getCurrentUser();
        PageInfo<SearchType> page = searchTypeService.findPageByUserId(user.getId(),pageNum,pageSize);
        return ApiResponse.success(page);
    }

    @PostMapping
    public ApiResponse save(@RequestBody SearchType searchType) {
        User user = springUtils.getCurrentUser();
        searchType.setUserId(user.getId());
        searchTypeService.save(searchType);
        return ApiResponse.success();
    }

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        searchTypeService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/query/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        SearchType searchType = searchTypeService.findById(id);
        return ApiResponse.success(searchType);
    }
}
