package com.wxy.web.favorites.security;

import com.wxy.web.favorites.constant.PublicConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@Slf4j
public class TokenUtil {

    @Value("${jwt.token-secret-key:}")
    private String tokenSecretKey;

    @Value("${jwt.token-expired-seconds:7200}")
    private long tokenExpiredSeconds;

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("token解析失败");
        }
        return null;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(StringUtils.isNoneBlank(tokenSecretKey) ? tokenSecretKey : PublicConstants.DEFAULT_TOKEN_SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, TimeUnit.SECONDS.toMillis(tokenExpiredSeconds));
    }

    public String generateToken(String username, long time) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, time);
    }

    private String createToken(Map<String, Object> claims, String subject, long time) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .signWith(SignatureAlgorithm.HS256, StringUtils.isNoneBlank(tokenSecretKey) ? tokenSecretKey : PublicConstants.DEFAULT_TOKEN_SECRET_KEY).compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

