package com.wxy.web.favorites.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.wxy.web.favorites.util.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * @author wangxiaoyuan
 * 2021/1/5 11:25
 **/
@RestController
@RequestMapping("/search")
public class SearchController {

    @Value("${app.data-url}")
    private String dataUrl;

    @GetMapping("/data")
    public ApiResponse data() {
        JSONArray jsonArray = JSONUtil.readJSONArray(FileUtil.file(dataUrl), StandardCharsets.UTF_8);
        return ApiResponse.success(jsonArray);
    }
}
