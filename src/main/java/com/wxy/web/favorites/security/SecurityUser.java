package com.wxy.web.favorites.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/***
 * <p>
 * Description: 自定义用户对象，方便后期业务拓展
 * </p>
 * @author wangxiaoyuan
 * 2021年12月10日
 */
@Getter
public class SecurityUser extends User {

    private final Integer id;

    public SecurityUser(Integer id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}
