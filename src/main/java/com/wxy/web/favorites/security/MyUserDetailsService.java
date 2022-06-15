package com.wxy.web.favorites.security;

import com.wxy.web.favorites.constant.DataConstants;
import com.wxy.web.favorites.dao.UserRepository;
import com.wxy.web.favorites.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/***
 * <p>
 * Description: 加载用户信息处理
 * </p>
 * @author wangxiaoyuan
 * 2021年12月08日
 */
@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Value("${admin.username}")
    private String adminUsername;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user != null && !Objects.equals(user.getStatus(), 2)) {
            /*
             * 此处查询用户权限，方便后续接口权限校验
             * 若接口需要开启权限验证，则在对应接口上配置 @Secured("xxx")注解
             * */
            List<GrantedAuthority> authorities;
            if (Objects.equals(adminUsername, user.getUsername())) {
                authorities = DataConstants.SUPER_ADMIN_ROLE_LIST.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            } else if (Objects.equals(user.getAdmin(), 1)) {
                authorities = DataConstants.ADMIN_ROLE_LIST.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            } else {
                authorities = DataConstants.USER_ROLE_LIST.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            }
            return new SecurityUser(user.getId(), user.getUsername(), user.getPassword(), authorities);
        }
        throw new UsernameNotFoundException("用户不存在");
    }
}
