package com.wxy.web.favorites.util;

import java.util.Random;

public class PasswordUtils {

    private static final String SCOPE = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890!@#$%^&*";

    private static final Random R = new Random();

    /**
     * 生成随机密码
     *
     * @param length
     * @return
     */
    public static String randomPassword(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(SCOPE.charAt(R.nextInt(SCOPE.length())));
        }
        return sb.toString();
    }
}