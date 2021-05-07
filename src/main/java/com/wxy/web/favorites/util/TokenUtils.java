package com.wxy.web.favorites.util;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.exception.NoLoginException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author wangxiaoyuan
 * 2021/5/7 10:45
 **/
@Component
@Data
public class TokenUtils {

    @Value("${token.token-secret-key}")
    private String tokenSecretKey;

    @Value("${token.token-expired-seconds}")
    private long tokenExpiredSeconds;

    @Value("${token.token-header}")
    private String tokenHeader;

    @Value("${token.token-prefix}")
    private String tokenPrefix;

    public String createToken(Integer userId) {
        return createToken(userId, TimeUnit.SECONDS.toMillis(tokenExpiredSeconds));
    }

    public String createToken(Integer userId, long time) {
        try {
            long exp = System.currentTimeMillis() + time;
            String encrypt = AESUtils.encrypt(userId + PublicConstants.TOKEN_DELIMITER + exp, tokenSecretKey);
            return tokenPrefix + " " + encrypt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer checkToken(String token) {
        try {
            if (StringUtils.isNotBlank(token) && token.startsWith(tokenPrefix)) {
                String message = token.substring(tokenPrefix.length() + 1);
                String decrypt = AESUtils.decrypt(message, tokenSecretKey);
                String[] split = decrypt.split(PublicConstants.TOKEN_DELIMITER);
                boolean exp = Long.parseLong(split[1]) < System.currentTimeMillis();
                if (StringUtils.isNotBlank(split[0]) && !exp) {
                    return Integer.valueOf(split[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
