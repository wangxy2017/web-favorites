package com.wxy.web.favorites.config;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.wxy.web.favorites.constant.PublicConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年10月12日
 */
@Configuration
public class AesConfig {

    @Value("${aes.secret-key:}")
    private String secretKey;

    @Bean
    public AES aes() {
        return SecureUtil.aes((StringUtils.isNotBlank(secretKey) ? secretKey : PublicConstants.DEFAULT_AES_SECRET_KEY).getBytes(StandardCharsets.UTF_8));
    }
}
