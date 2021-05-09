package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.VerificationRepository;
import com.wxy.web.favorites.model.Verification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wangxiaoyuan
 * 2021/5/7 13:41
 **/
@Service
public class VerificationService {

    @Autowired
    private VerificationRepository verificationRepository;

    public Verification findCode(String account, Integer action) {
        return verificationRepository.findTopByAccountAndActionOrderByExpiredTimeDesc(account, action);
    }

    public Verification save(Verification verification) {
        return verificationRepository.save(verification);
    }
}
