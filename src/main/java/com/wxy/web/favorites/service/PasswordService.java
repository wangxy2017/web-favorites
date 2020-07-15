package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.PasswordRepository;
import com.wxy.web.favorites.model.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
public class PasswordService {

    @Autowired
    private PasswordRepository passwordRepository;

    public Password findByFavoritesId(Integer favoritesId) {
        return passwordRepository.findByFavoritesId(favoritesId);
    }

    public Password save(Password password) {
        return passwordRepository.save(password);
    }

    public void deleteById(Integer id) {
        passwordRepository.deleteById(id);
    }
}
