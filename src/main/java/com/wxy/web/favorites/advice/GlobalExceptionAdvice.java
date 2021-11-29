package com.wxy.web.favorites.advice;

import cn.hutool.core.util.IdUtil;
import com.wxy.web.favorites.core.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = Exception.class)
    public ApiResponse Exception(Exception e) {
        String errorId = IdUtil.simpleUUID();
        log.error("内部错误：errorId = {}", errorId, e);
        return ApiResponse.error("内部错误：errorId = " + errorId);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ApiResponse HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ApiResponse.error(400, "参数解析失败");
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ApiResponse IllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ApiResponse HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.error(405, "不支持当前请求方法");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult exceptions = e.getBindingResult();
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                FieldError fieldError = (FieldError) errors.get(0);
                return ApiResponse.error(fieldError.getDefaultMessage());
            }
        }
        return ApiResponse.error(e.getMessage());
    }
}
