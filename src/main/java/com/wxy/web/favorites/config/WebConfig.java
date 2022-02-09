package com.wxy.web.favorites.config;

import com.wxy.web.favorites.interceptor.RateLimiterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <p>
 *
 * </p>
 *
 * @author e-Xiaoyuan.Wang
 * @since 2022/2/9 13:41
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimiterInterceptor())
                .addPathPatterns("/**");
    }
}
