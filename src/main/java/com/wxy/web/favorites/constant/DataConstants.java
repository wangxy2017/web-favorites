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

    static {
        RECOMMEND_LIST = JSONUtil.toList(JSONUtil.parseArray(ResourceUtil.readStr("data/recommend.json", StandardCharsets.UTF_8)), RecommendDto.class);
        SEARCH_LIST = JSONUtil.toList(JSONUtil.parseArray(ResourceUtil.readStr("data/search.json", StandardCharsets.UTF_8)), SearchDto.class);
        SYSTEM_NOTICE = JSONUtil.toBean(JSONUtil.parseObj(ResourceUtil.readStr("data/notice.json", StandardCharsets.UTF_8)), NoticeDto.class);
    }
}
