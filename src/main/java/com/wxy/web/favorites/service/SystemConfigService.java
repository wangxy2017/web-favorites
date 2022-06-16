package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.SystemConfigRepository;
import com.wxy.web.favorites.model.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangxiaoyuan
 * 2021/5/7 13:41
 **/
@Service
@Transactional
public class SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    public List<SystemConfig> findAll() {
        return systemConfigRepository.findAll();
    }

    public SystemConfig findByKeyCode(String keyCode) {
        return systemConfigRepository.findByKeyCode(keyCode);
    }

    public void save(SystemConfig config) {
        systemConfigRepository.save(config);
    }

    public List<SystemConfig> findByKeyCodeIn(List<String> list) {
        return systemConfigRepository.findByKeyCodeIn(list);
    }
}
