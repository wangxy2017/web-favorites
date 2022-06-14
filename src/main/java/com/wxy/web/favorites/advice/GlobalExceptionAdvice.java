package com.wxy.web.favorites.advice;

import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.ErrorId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ApiResponse AccessDeniedException(AccessDeniedException e) {
        return ApiResponse.error(ErrorConstants.NO_PERMISSION_CODE, ErrorConstants.NO_PERMISSION_MSG);
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public ApiResponse HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.error("不支持当前请求方法");
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
        return ApiResponse.error("参数验证失败");
    }
}
