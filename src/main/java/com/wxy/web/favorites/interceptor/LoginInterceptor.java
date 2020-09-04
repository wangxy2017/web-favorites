package com.wxy.web.favorites.interceptor;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.SpringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final Base64.Decoder decoder = Base64.getDecoder();

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        User user = (User) request.getSession().;
        if (user != null) {
            return true;
        } else {
            // 查看cookie
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals("token")) {
                        String[] token = new String(decoder.decode(cookie.getValue())).split("&&");
                        User user1 = userRepository.findByUsername(token[0]);
                        if (user1 != null && user1.getPassword().equals(token[1])) {
                            request.getSession().setAttribute("user", user1);
                            return true;
                        }
                    }
                }

            }
            response.sendRedirect(request.getContextPath() + "/login.html");
        }
        return false;
    }
}
