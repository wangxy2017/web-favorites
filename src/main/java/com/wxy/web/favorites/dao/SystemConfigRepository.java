package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.SystemConfig;
import com.wxy.web.favorites.model.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author wangxiaoyuan
 * 2021/5/7 13:34
 **/
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Integer>, JpaSpecificationExecutor<SystemConfig> {

    SystemConfig findByKeyCode(String keyCode);
}
