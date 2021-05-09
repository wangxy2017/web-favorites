package com.wxy.web.favorites.constant;

/**
 * @author wangxiaoyuan
 * 2021/5/7 16:15
 **/
public class PublicConstants {
    public static final String DEFAULT_CATEGORY_NAME = "默认分类";

    public static final Integer TASK_LEVEL_0 = 0;
    public static final Integer TASK_LEVEL_1 = 1;
    public static final Integer TASK_LEVEL_2 = 2;
    public static final Integer TASK_LEVEL_3 = 3;
    public static final Integer TASK_LEVEL_4 = 4;
    public static final Integer TASK_LEVEL_5 = 5;

    public static final String FAVORITES_ICON_DEFAULT = "images/book.svg";

    public static final String FORMAT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String FORMAT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_MINUTE_PATTERN = "yyyy-MM-dd HH:mm";

    public static final Integer VERIFICATION_REGISTER = 0;
    public static final Integer VERIFICATION_EMAIL_LOGIN = 1;
    public static final Integer VERIFICATION_EMAIL_UPDATE = 2;

    public static final Integer REMEMBER_ME_DAYS = 14;
    public static final String REMEMBER_ME_CODE = "1";

    public static final Integer RANDOM_CODE_LENGTH = 6;

    public static final Integer USER_SECRET_KEY_LENGTH = 16;

    public static final Integer TEMP_PASSWORD_LENGTH = 8;

    public static final String ID_DELIMITER = ",";

    public static final String DEFAULT_DELIMITER = ",";

    public static final Integer DIR_CODE = 1;
    public static final Integer SYSTEM_CATEGORY_CODE = 1;
    public static final Integer BOOKMARK_STYLE_CODE = 1;
    public static final Integer FAVORITES_STAR_CODE = 1;
    public static final Integer TASK_ALARM_CODE = 1;
    public static final Integer MOMENT_TOP_CODE = 1;

    public static final Integer MAX_SORT_NUMBER = 9999;

    public static final String EXPORT_FAVORITES_CODE = "1";
    public static final String EXPORT_MOMENT_CODE = "1";
    public static final String EXPORT_TASK_CODE = "1";
    public static final String EXPORT_SEARCH_CODE = "1";

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final int SUCCESS_CODE = 0;
    public static final String SUCCESS_MSG = "SUCCESS";

    public static final int ERROR_CODE = -1;
    public static final String ERROR_MSG = "ERROR";

    public static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    public static final String CONTENT_TYPE_STREAM = "application/octet-stream";


    public static final String FAVORITES_STAR_LIMITED_MSG = "标记数量已达上限";
}
