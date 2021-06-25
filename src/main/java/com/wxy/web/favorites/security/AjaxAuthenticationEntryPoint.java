package com.wxy.web.favorites.security;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.util.ApiResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author wangxiaoyuan
 * 2021/5/8 10:16
 **/
@Component
public class AjaxAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        ApiResponse response = new ApiResponse(ErrorConstants.NO_LOGIN_CODE, ErrorConstants.NO_LOGIN_MSG, null);
        httpServletResponse.setStatus(HttpStatus.HTTP_OK);
        httpServletResponse.setContentType(PublicConstants.CONTENT_TYPE_JSON);
        httpServletResponse.getWriter().write(JSONObject.toJSONString(response));
    }
}
