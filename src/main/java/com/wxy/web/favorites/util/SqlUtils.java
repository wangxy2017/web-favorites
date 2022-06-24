package com.wxy.web.favorites.util;

import java.util.List;

public class SqlUtils {

    private static final List<String> CHARS = List.of("%", "_", "\\");

    public static String trimAndEscape(String keyword) {
        if (keyword == null) {
            return "";
        }
        keyword = keyword.trim();
        if (keyword.length() == 0) {
            return keyword;
        }
        for (String s : CHARS) {
            if (keyword.startsWith(s)) {
                return "\\" + keyword;
            }
        }
        return keyword;
    }
}
