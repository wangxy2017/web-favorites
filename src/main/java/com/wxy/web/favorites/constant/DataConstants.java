package com.wxy.web.favorites.constant;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.wxy.web.favorites.dto.NoticeDto;
import com.wxy.web.favorites.dto.RecommendDto;
import com.wxy.web.favorites.dto.SearchDto;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author e-Xiaoyuan.Wang
 * @since 2022/4/7 14:41
 */
public class DataConstants {

    public static final List<RecommendDto> RECOMMEND_LIST;
    public static final List<SearchDto> SEARCH_LIST;
    public static final NoticeDto SYSTEM_NOTICE;
    public static final List<String> USER_PERMISSION_LIST = List.of("user", "task", "share", "navigation", "password",
            "moment", "memorandum", "file", "favorites", "category");
    public static final List<String> ADMIN_PERMISSION_LIST = List.of("system_config", "admin_index", "system_notice",
            "operation_log");
    public static final List<String> SUPER_ADMIN_PERMISSION_LIST = List.of("system_config", "admin_index", "system_notice",
            "admin", "admin_user", "operation_log");

    static {
        RECOMMEND_LIST = JSONUtil.toList(JSONUtil.parseArray(ResourceUtil.readStr("data/recommend.json", StandardCharsets.UTF_8)), RecommendDto.class);
        SEARCH_LIST = JSONUtil.toList(JSONUtil.parseArray(ResourceUtil.readStr("data/search.json", StandardCharsets.UTF_8)), SearchDto.class);
        SYSTEM_NOTICE = JSONUtil.toBean(JSONUtil.parseObj(ResourceUtil.readStr("data/notice.json", StandardCharsets.UTF_8)), NoticeDto.class);
    }
}
