package com.wxy.web.favorites.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
public class RecommendsConfig {

    private List<String> recommends;

    public List<String> getRecommends() {
        return recommends;
    }

    public void setRecommends(List<String> recommends) {
        this.recommends = recommends;
    }
}
