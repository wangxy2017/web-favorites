package com.wxy.web.favorites.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/***
 * <p>
 * Description: Spring Security全局配置
 * </p>
 * @author wangxiaoyuan
 * 2021年12月08日
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    @Autowired
    private MyAuthenticationEntryPoint myAuthenticationEntryPoint;
    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;
    @Autowired
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    /**
     * 加密算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService);
    }

    // 授权时去掉默认角色前缀"ROLE_"
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().disable()// 禁用表单登录，前后端分离用不上
                .logout().disable()// 禁用默认退出接口
                .authorizeRequests().antMatchers("/").permitAll()// 允许根路径url的访问
                .antMatchers("/login/**", "/register/**", "/file/share/download/**", "/websocket/**").permitAll()
                .anyRequest().authenticated()// 其他请求需要认证
                .and().exceptionHandling()
                .authenticationEntryPoint(myAuthenticationEntryPoint)// 认证未通过，不允许访问异常处理器
                .accessDeniedHandler(myAccessDeniedHandler)// 认证通过，但是没权限处理器
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)// 禁用session，JWT校验不需要session
                .and().addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)// 将TOKEN校验过滤器配置到过滤器链中，否则不生效，放到UsernamePasswordAuthenticationFilter之前
                .csrf().disable()// 关闭csrf
                .headers().frameOptions().disable();// 允许iframe嵌套
    }

    /**
     * 静态资源配置
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                //swagger2所需要用到的静态资源，允许访问
                .antMatchers("/swagger-ui.html")
                .antMatchers("/swagger-ui/**")
                .antMatchers("/swagger-resources/**")
                .antMatchers("/webjars/**")
                .antMatchers("/v2/**")
                // h2控制台
                .antMatchers("/h2-console/**")
                // 其他静态资源
                .antMatchers("/login.html", "/qrLogin.html", "/index.html", "/search.html",
                        "/moment.html", "/calendar.html", "/moment_edit.html", "/file.html", "/recycle.html",
                        "/report.html", "/memorandum.html", "/share.html", "/layui/**", "/images/**",
                        "/css/**", "/js/**", "/plugin/**", "/favicon.ico", "/admin_index.html", "/admin.html",
                        "/admin_user.html", "/admin_report.html", "/system_notice.html", "/system_config.html",
                        "/set_user_info.html", "/operation_log.html", "/send_mail_edit.html");
    }
}