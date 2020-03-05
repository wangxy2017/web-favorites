package com.wxy.web.favorites.exception;

import com.wxy.web.favorites.util.ApiResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ApiResponse exceptionHandle(Exception e) {
        e.printStackTrace();
        return new ApiResponse(500, "系统错误", e.getMessage());
    }
}
