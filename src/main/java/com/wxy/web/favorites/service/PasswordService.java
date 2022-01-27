package com.wxy.web.favorites.service;

import cn.hutool.crypto.symmetric.AES;
import com.wxy.web.favorites.dao.PasswordRepository;
import com.wxy.web.favorites.model.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class PasswordService {

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private AES aes;

    public Password findByFavoritesId(Integer favoritesId) {
        Password password = passwordRepository.findByFavoritesId(favoritesId);
        if (password != null) {
            Password password1 = new Password(password.getId(), password.getAccount(), password.getPassword(), password.getFavoritesId(),null);
            Optional.ofNullable(password1.getAccount()).ifPresent(a -> password1.setAccount(aes.decryptStr(a)));
            Optional.ofNullable(password1.getPassword()).ifPresent(p -> password1.setPassword(aes.decryptStr(p)));
            return password1;
        } else {
            return null;
        }
    }

    public Password save(Password password) {
        Optional.ofNullable(password.getAccount()).ifPresent(a -> password.setAccount(aes.encryptBase64(a)));
        Optional.ofNullable(password.getPassword()).ifPresent(p -> password.setPassword(aes.encryptBase64(p)));
        return passwordRepository.save(password);
    }

    public void deleteById(Integer id) {
        passwordRepository.deleteById(id);
    }
}
