package com.wxy.web.favorites.constant;

/**
 * @author wangxiaoyuan
 * 2021/5/7 16:15
 **/
public interface PublicConstants {
    String DEFAULT_CATEGORY_NAME = "默认分类";

    Integer TASK_LEVEL_0 = 0;
    Integer TASK_LEVEL_1 = 1;
    Integer TASK_LEVEL_2 = 2;
    Integer TASK_LEVEL_3 = 3;
    Integer TASK_LEVEL_4 = 4;
    Integer TASK_LEVEL_5 = 5;

    String FAVORITES_ICON_DEFAULT = "images/book.svg";

    String FORMAT_DATE_PATTERN = "yyyy-MM-dd";
    String FORMAT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    Integer VERIFICATION_REGISTER = 0;
    Integer VERIFICATION_EMAIL_LOGIN = 1;
    Integer VERIFICATION_EMAIL_UPDATE = 2;
    Integer VERIFICATION_EMAIL_FORGOT = 3;

    Integer REMEMBER_ME_DAYS = 14;
    String REMEMBER_ME_CODE = "1";

    Integer RANDOM_CODE_LENGTH = 6;

    Integer TEMP_PASSWORD_LENGTH = 8;

    String ID_DELIMITER = ",";

    Integer DELETE_CODE = 1;

    String DEFAULT_DELIMITER = ",";

    Integer DIR_CODE = 1;
    Integer SYSTEM_CATEGORY_CODE = 1;
    Integer BOOKMARK_STYLE_CODE = 1;
    Integer FAVORITES_STAR_CODE = 1;
    Integer TASK_ALARM_CODE = 1;
    Integer MOMENT_TOP_CODE = 1;

    Integer MAX_SORT_NUMBER = 9999;

    String EXPORT_FAVORITES_CODE = "1";
    String EXPORT_MOMENT_CODE = "1";
    String EXPORT_TASK_CODE = "1";
    String EXPORT_SEARCH_CODE = "1";

    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";

    Integer SUCCESS_CODE = 0;
    String SUCCESS_MSG = "SUCCESS";

    Integer ERROR_CODE = -1;
    String ERROR_MSG = "ERROR";

    String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    String CONTENT_TYPE_STREAM = "application/octet-stream";

    String DEFAULT_TOKEN_SECRET_KEY = "wdmhm7iJvRf2n#hc";

    String DEFAULT_AES_SECRET_KEY = "4sn2UaVa96pj4n#V";

    String FAVORITES_STAR_LIMITED_MSG = "标记数量已达上限";

    String NAVIGATION_LIMITED_MSG = "快捷导航数量已达上限";

    Integer SHARE_CODE = 1;
    String EXPORT_QUICK_NAVIGATION = "1";
    Double DISK_LIMIT_RATE = 0.9;
}
