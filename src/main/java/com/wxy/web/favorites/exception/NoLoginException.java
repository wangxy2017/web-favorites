package com.wxy.web.favorites.exception;

/**
 * @author wangxiaoyuan
 * 2020/12/24 15:33
 **/
public class NoLoginException extends RuntimeException {

    public NoLoginException() {
        super();
    }

    public NoLoginException(String msg) {
        super(msg);
    }
}
