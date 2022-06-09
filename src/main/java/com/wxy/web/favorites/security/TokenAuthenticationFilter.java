package com.wxy.web.favorites.security;

import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年12月09日
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = Optional.ofNullable(request.getHeader(PublicConstants.TOKEN_HEADER)).filter(t -> t.startsWith(PublicConstants.TOKEN_PREFIX)).map(t -> t.replace(PublicConstants.TOKEN_PREFIX, "")).orElse(null);
        if (StrUtil.isNotBlank(token)) {
            String username = tokenUtils.getUsernameFromToken(token);
            if (StrUtil.isNotBlank(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
                // 验证token是否有效
                if (tokenUtils.validateToken(token, userDetails)) {
                    // 将用户信息存入authentication，方便后续校验
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 将authentication存入上下文，方便后续获取用户信息
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // 继续执行下一个过滤器
        filterChain.doFilter(request, response);
    }
}
