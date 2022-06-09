package com.wxy.web.favorites.util;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResponseUtils {

    /**
     * 向客户端写入json格式数据
     *
     * @param response
     * @param obj
     * @throws IOException
     */
    public static void writeJson(HttpServletResponse response, Object obj) throws IOException {
        response.setStatus(HttpStatus.HTTP_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(ContentType.JSON.getValue());
        response.getWriter().write(JSONUtil.toJsonStr(obj));
    }

    /**
     * 向客户端写入文本格式数据
     *
     * @param response
     * @param obj
     * @throws IOException
     */
    public static void writeText(HttpServletResponse response, Object obj) throws IOException {
        response.setStatus(HttpStatus.HTTP_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(ContentType.TEXT_PLAIN.getValue());
        response.getWriter().write(obj == null ? "" : obj.toString());
    }
}
