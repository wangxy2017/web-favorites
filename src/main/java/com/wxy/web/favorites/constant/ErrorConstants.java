package com.wxy.web.favorites.constant;

/**
 * @author wangxiaoyuan
 * 2021/5/8 12:42
 **/
public interface ErrorConstants {

     int NO_LOGIN_CODE = 401;
     String NO_LOGIN_MSG = "token过期，请重新登录！";

     int NO_PERMISSION_CODE = 403;
     String NO_PERMISSION_MSG = "权限不足！";

     String SYSTEM_CATEGORY_NO_DELETE_MSG = "系统分类无法删除";

     String ILLEGAL_OPERATION_MSG = "非法操作";

     String NO_SPACE_LEFT_MSG = "剩余空间不足";

     String FILE_READ_FAILED_MSG = "文件读取异常";

     String INVALID_VERIFICATION_MSG = "验证码错误";
     String INVALID_USERNAME_OR_PASSWORD_MSG = "用户名或密码错误";
     String INVALID_USERNAME_MSG = "请先注册账号";
     String INVALID_USERNAME_OR_EMAIL_MSG = "账号或邮箱不存在";
     String INVALID_PASSWORD_MSG = "密码错误";

     String USERNAME_OR_EMAIL_EXISTED_MSG = "用户名或邮箱已存在";

     String QRCODE_INVALID_MSG = "二维码已失效";
     String SID_NOT_FOUND_MSG = "缺少必要参数：sid";

     String RESOURCE_NOT_FOUND_MSG = "资源不存在";
     String FILE_IS_DELETED_MSG = "文件被物理删除";
     String USER_DISABLED_MSG = "您的账号已锁定，可通过忘记密码解锁";
}
