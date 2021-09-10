package com.wxy.web.favorites.service;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.VerificationRepository;
import com.wxy.web.favorites.model.Verification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author wangxiaoyuan
 * 2021/5/7 13:41
 **/
@Service
@Transactional
public class VerificationService {

    @Autowired
    private VerificationRepository verificationRepository;

    public Verification findCode(String account, Integer action) {
        return verificationRepository.findTopByAccountAndActionOrderByExpiredTimeDesc(account, action);
    }

    public Verification save(Verification verification) {
        return verificationRepository.save(verification);
    }

    public void deleteBeforeTime(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
        verificationRepository.deleteByExpiredTimeBefore(sdf.parse(time));
    }

    public void deleteById(Integer id){
        verificationRepository.deleteById(id);
    }
}
