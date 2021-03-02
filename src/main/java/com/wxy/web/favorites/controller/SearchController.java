package com.wxy.web.favorites.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.wxy.web.favorites.model.SearchType;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.SearchTypeService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangxiaoyuan
 * 2021/1/5 11:25
 **/
@RestController
@RequestMapping("/search")
public class SearchController {

    @Value("${app.data-url}")
    private String dataUrl;

    @Autowired
    private SearchTypeService searchTypeService;

    @Autowired
    private SpringUtils springUtils;

    @GetMapping("/data")
    public ApiResponse data() {
        // 查询系统搜索引擎
        JSONArray jsonArray = JSONUtil.readJSONArray(new File(dataUrl), StandardCharsets.UTF_8);
        List<SearchType> systemList = jsonArray.toList(SearchType.class);
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
