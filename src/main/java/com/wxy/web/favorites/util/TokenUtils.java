package com.wxy.web.favorites.util;

import com.wxy.web.favorites.exception.NoLoginException;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author wangxiaoyuan
 * 2021/5/7 10:45
 **/
public class TokenUtils {

    public static final String TOKEN_SECRET_KEY = "Kjykag2^CMpBr5Zi";

    public static final long TOKEN_EXPIRED_SECONDS = 15;

    public static final String TOKEN_DELIMITER = "/";

    public static final String TOKEN_HEAD = "Authorization";


    public static String createToken(Integer userId) {
        try {
            long exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(TOKEN_EXPIRED_SECONDS);
            return AESUtils.encrypt(userId + TOKEN_DELIMITER + exp, TOKEN_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer checkToken(String token) {
        try {
            String decrypt = AESUtils.decrypt(token, TOKEN_SECRET_KEY);
            String[] split = decrypt.split(TOKEN_DELIMITER);
            boolean exp = Long.parseLong(split[1]) < System.currentTimeMillis();
            if (StringUtils.isNotBlank(split[0]) && !exp) {
                return Integer.valueOf(split[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NoLoginException();
    }
}
