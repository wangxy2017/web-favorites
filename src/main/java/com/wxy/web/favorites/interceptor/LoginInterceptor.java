package com.wxy.web.favorites.interceptor;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.exception.NoLoginException;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.AESUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Base64;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Value("${aes-key}")
    private String aesKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().equals("/")) {
            response.sendRedirect("index.html");
        }
        HttpSession session = request.getSession();
        if (session.getAttribute("login_user") != null) {
            return true;
        } else {
            // 查看cookie
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals("token")) {
                        String tokenValue = null;
                        try {
                            tokenValue = AESUtils.decrypt(cookie.getValue(), aesKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (tokenValue != null && tokenValue.contains("&&")) {
                            String[] token = tokenValue.split("&&");
                            User user1 = userRepository.findByUsername(token[0]);
                            if (user1 != null && user1.getPassword().equals(token[1])) {
                                session.setAttribute("login_user", user1);
                                return true;
                            }
                        }
                    }
                }

            }
            throw new NoLoginException();
        }
    }
}
