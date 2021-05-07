package com.wxy.web.favorites.util;

import com.wxy.web.favorites.exception.NoLoginException;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SpringUtils {

    @Autowired
    private UserService userService;

    public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public User getCurrentUser() {
        Object userId = getRequest().getAttribute("user_id");
        if (userId == null) {
            throw new NoLoginException();
        }
        User user = userService.findById(Integer.valueOf(userId.toString()));
        if (user == null) {
            throw new NoLoginException();
        }
        return user;
    }
}
