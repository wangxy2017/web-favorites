package com.wxy.web.favorites.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.model.SearchType;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.SearchTypeService;
import com.wxy.web.favorites.util.ApiResponse;
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
    public ApiResponse data() {
        // 查询系统搜索引擎
        List<SearchType> systemList = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource(appConfig.getSearchTypeJson());
            InputStream input = resource.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(input);
            String json = new String(bytes, StandardCharsets.UTF_8);
            JSONArray jsonArray = JSONUtil.parseArray(json);
            systemList = jsonArray.toList(SearchType.class);
        } catch (IOException e) {
            log.error("文件读取错误：{}", appConfig.getSearchTypeJson(), e);
        }
        // 查询自定义搜索引擎
        User user = springUtils.getCurrentUser();
        List<SearchType> list = searchTypeService.findByUserId(user.getId());
        Map<String, List<SearchType>> data = new HashMap<>();
        data.put("systemList", systemList);
        data.put("userList", list);
        return ApiResponse.success(data);
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
