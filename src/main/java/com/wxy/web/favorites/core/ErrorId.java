package com.wxy.web.favorites.core;

import cn.hutool.core.util.IdUtil;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ErrorId {

    private final String errorId;

    private ErrorId() {
        errorId = IdUtil.simpleUUID();
    }

    public static ErrorId get() {
        return new ErrorId();
    }
}
