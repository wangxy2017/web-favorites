package com.wxy.web.favorites.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统参数配置
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {

    private Integer fileDeepLevel;

    private Integer recycleSaveDays;

    private Integer starLimit;

    private Integer navigationLimit;

    private Integer favoritesSearchLimit;

    private Integer categorySearchLimit;

    private Integer favoritesLimit;

    private String searchTypeJson;

    private Integer initCapacity;

    private String fileRepository;

    private String fileSuffixes;

    private Integer fileCompressLevel;

    private Integer verificationExpiredMinutes;

    private Integer verificationResendSeconds;

    private Integer errorCountLimit;

}
