package com.wxy.web.favorites.core;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wxy.web.favorites.constant.PublicConstants;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonPropertyOrder(value = "code,msg,data")
public class ApiResponse {
    Integer code;
    String msg;
    Object data;

    public static ApiResponse success() {
        return new ApiResponse(PublicConstants.SUCCESS_CODE, PublicConstants.SUCCESS_MSG, null);
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(PublicConstants.SUCCESS_CODE, PublicConstants.SUCCESS_MSG, data);
    }

    public static ApiResponse error() {
        return new ApiResponse(PublicConstants.ERROR_CODE, PublicConstants.ERROR_MSG, null);
    }

    public static ApiResponse error(String msg) {
        return new ApiResponse(PublicConstants.ERROR_CODE, msg, null);
    }

    public static ApiResponse error(Integer code, String msg) {
        return new ApiResponse(code, msg, null);
    }

    public static ApiResponse error(String msg, Object data) {
        return new ApiResponse(PublicConstants.ERROR_CODE, msg, null);
    }

}
