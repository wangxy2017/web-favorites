package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.SecretKeyRepository;
import com.wxy.web.favorites.model.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class SecretKeyService {

    @Autowired
    private SecretKeyRepository secretKeyRepository;

    public SecretKey findByUserId(Integer userId) {
        return secretKeyRepository.findByUserId(userId);
    }

    public SecretKey save(SecretKey secretKey) {
        return secretKeyRepository.save(secretKey);
    }

}
