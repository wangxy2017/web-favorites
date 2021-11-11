package com.wxy.web.favorites.util;

import cn.hutool.core.lang.UUID;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.wf.captcha.SpecCaptcha;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CaptchaUtils {

    private static final Cache<String, Object> timedCache = CacheBuilder.newBuilder()
            .maximumSize(1000) // 设置缓存的最大容量
            .expireAfterWrite(1, TimeUnit.MINUTES) // 设置缓存在写入一分钟后失效
            .concurrencyLevel(10) // 设置并发级别为10
            .recordStats() // 开启缓存统计
            .build();

    public static Map<String, String> generate() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        String verCode = specCaptcha.text().toLowerCase();
        String key = UUID.fastUUID().toString();
        timedCache.put(key, verCode);
        Map<String, String> map = Maps.newHashMap();
        map.put("key", key);
        map.put("image", specCaptcha.toBase64());
        return map;
    }

    public static boolean verify(String key, String code) {
        Object verCode = timedCache.getIfPresent(key);
        if (verCode != null && code.equalsIgnoreCase(verCode.toString())) {
            timedCache.invalidate(key);
            return true;
        }
        return false;
    }
}
