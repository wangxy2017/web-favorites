package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.QuickNavigationRepository;
import com.wxy.web.favorites.model.QuickNavigation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class QuickNavigationService {

    @Autowired
    private QuickNavigationRepository quickNavigationRepository;

    public QuickNavigation save(QuickNavigation quickNavigation) {
        return quickNavigationRepository.save(quickNavigation);
    }

    public List<QuickNavigation> findList(Integer userId){
        return quickNavigationRepository.findAllByUserId(userId);
    }

}

