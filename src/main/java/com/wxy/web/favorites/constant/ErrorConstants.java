package com.wxy.web.favorites.constant;

/**
 * @author wangxiaoyuan
 * 2021/5/8 12:42
 **/
public class ErrorConstants {

    public static final int NO_LOGIN_CODE = 401;
    public static final String NO_LOGIN_MSG = "token过期，请重新登录";

    public static final String SYSTEM_CATEGORY_NO_DELETE_MSG = "系统分类无法删除";

    public static final String ILLEGAL_OPERATION_MSG = "非法操作";

    public static final String NO_SPACE_LEFT_MSG = "剩余空间不足";

    public static final String FILE_READ_FAILED_MSG = "文件读取异常";

    public static final String INVALID_VERIFICATION_MSG = "验证码错误";
    public static final String INVALID_USERNAME_OR_PASSWORD_MSG = "用户名或密码错误";
    public static final String INVALID_USERNAME_MSG = "请先注册账号";
    public static final String INVALID_USERNAME_OR_EMAIL_MSG = "账号或邮箱不存在";
    public static final String INVALID_PASSWORD_MSG = "密码错误";

    public static final String USERNAME_OR_EMAIL_EXISTED_MSG = "用户名或邮箱已存在";

    public static final String QRCODE_INVALID_MSG = "二维码已失效";
    public static final String SID_NOT_FOUND = "缺少必要参数：sid";

    public static final String RESOURCE_NOT_FOUND = "资源不存在";
    public static final String FILE_IS_DELETED = "文件被物理删除";
}
