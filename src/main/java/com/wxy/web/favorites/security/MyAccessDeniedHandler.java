package com.wxy.web.favorites.security;

import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.util.ResponseUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * <p>
 * Description: 权限不足处理
 * </p>
 * @author wangxiaoyuan
 * 2021年12月09日
 */
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        ResponseUtils.writeJson(response, ApiResponse.error(403, "权限不足！"));
    }
}
