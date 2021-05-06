package com.wxy.web.favorites.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * @author wangxiaoyuan
 * 2021/5/6 16:26
 **/
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * 静态资源放行
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/images/**", "/layui/**", "/favicon.ico", "/*.html");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                // 放行接口
                .antMatchers("/login/**", "/register/**").permitAll()
                .anyRequest().authenticated()
                // 禁用session
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // 禁用csrf
                .and().csrf().disable();
    }
}
