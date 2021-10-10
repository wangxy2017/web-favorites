package com.wxy.web.favorites.advice;

import com.wxy.web.favorites.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResponse ExceptionHandle(Exception e) {
        log.error("系统异常：{}", e.getMessage(), e);
        return ApiResponse.error(e.getMessage());
    }
}
