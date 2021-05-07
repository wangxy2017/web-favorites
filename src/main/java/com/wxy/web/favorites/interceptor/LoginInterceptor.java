package com.wxy.web.favorites.interceptor;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.exception.NoLoginException;
import com.wxy.web.favorites.util.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private TokenUtils tokenUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().equals("/")) {
            response.sendRedirect("index.html");
        }
        String token = request.getHeader(tokenUtils.getTokenHeader());
        if (StringUtils.isNotBlank(token)) {
            Integer userId = tokenUtils.checkToken(token);
            request.setAttribute("user_id", userId);
            return true;
        }
        throw new NoLoginException();
    }
}
