package com.wxy.web.favorites.exception;

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
    public ApiResponse exceptionHandle(Exception e) {
        log.error("系统异常：", e);
        return new ApiResponse(500, "系统错误", e.getMessage());
    }
}
