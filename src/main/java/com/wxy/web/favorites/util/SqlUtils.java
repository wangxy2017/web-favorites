package com.wxy.web.favorites.util;

import cn.hutool.core.util.StrUtil;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年11月11日
 */
public class SqlUtils {

    public static String trimAndEscape(String keyword) {
        StringBuilder sb = new StringBuilder(keyword.trim());
        for (int i = 0; i < sb.length(); i++) {
            String c = String.valueOf(sb.charAt(i));
            if ("%_\\".contains(c)) {
                sb.insert(i, "\\");
                i++;
            }
        }
        return sb.toString();
    }
}
