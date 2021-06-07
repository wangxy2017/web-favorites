package com.wxy.web.favorites.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {

    private Integer starLimit;

    private Integer searchLimit;

    private Integer favoritesLimit;

    private String searchTypeJson;

    private Integer initCapacity;

    private String fileRepository;

    private String fileSuffixes;

    private Integer fileCompressLevel;

    private List<String> recommends;

    private Integer verificationExpiredMinutes ;

}
