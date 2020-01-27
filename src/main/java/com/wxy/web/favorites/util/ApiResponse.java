package com.wxy.web.favorites.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse {
    Integer code;
    String msg;
    Object data;

    public static ApiResponse success() {
        return new ApiResponse(0, "success", null);
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(0, "success", data);
    }

    public static ApiResponse error() {
        return new ApiResponse(-1, "error", null);
    }

    public static ApiResponse error(String msg) {
        return new ApiResponse(0, msg, null);
    }
}
