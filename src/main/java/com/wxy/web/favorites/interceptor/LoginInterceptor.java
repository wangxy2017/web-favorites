package com.wxy.web.favorites.interceptor;

import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.exception.NoLoginException;
import com.wxy.web.favorites.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Base64;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final Base64.Decoder decoder = Base64.getDecoder();

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("login_user") != null) {
            return true;
        } else {
            // 查看cookie
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (cookie.getName().equals("token")) {
                        String[] token = new String(decoder.decode(cookie.getValue())).split("&&");
                        User user1 = userRepository.findByUsername(token[0]);
                        if (user1 != null && user1.getPassword().equals(token[1])) {
                            session.setAttribute("login_user", user1);
                            return true;
                        }
                    }
                }

            }
            throw new NoLoginException();
        }
    }
}
