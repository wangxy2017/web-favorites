package com.wxy.web.favorites.security;

import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.util.ResponseUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * <p>
 * Description: 未通过认证处理
 * </p>
 * @author wangxiaoyuan
 * 2021年12月09日
 */
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ResponseUtils.writeJson(response, ApiResponse.error(ErrorConstants.NO_LOGIN_CODE, ErrorConstants.NO_LOGIN_MSG));
    }
}
