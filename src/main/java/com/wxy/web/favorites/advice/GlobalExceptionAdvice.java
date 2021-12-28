package com.wxy.web.favorites.advice;

import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.ErrorId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = Exception.class)
    public ApiResponse Exception(Exception e) {
        ErrorId errorId = ErrorId.get();
        log.error("系统错误：{}", errorId, e);
        return ApiResponse.error("系统错误", errorId);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ApiResponse IllegalArgumentException(IllegalArgumentException e) {
        ErrorId errorId = ErrorId.get();
        log.error("参数错误：{}", errorId, e);
        return ApiResponse.error(e.getMessage(), errorId);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ApiResponse HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        ErrorId errorId = ErrorId.get();
        log.error("不支持当前请求方法：{}", errorId, e);
        return ApiResponse.error("不支持当前请求方法", errorId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse MethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult exceptions = e.getBindingResult();
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                FieldError fieldError = (FieldError) errors.get(0);
                ErrorId errorId = ErrorId.get();
                log.error("参数验证失败：{}", errorId, e);
                return ApiResponse.error(fieldError.getDefaultMessage(), errorId);
            }
        }
        ErrorId errorId = ErrorId.get();
        log.error("参数验证失败：{}", errorId, e);
        return ApiResponse.error("参数验证失败", errorId);
    }
}
