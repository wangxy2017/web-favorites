package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.SecretKey;
import com.wxy.web.favorites.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SecretKeyRepository extends JpaRepository<SecretKey, Integer> {
    SecretKey findByUserId(Integer userId);

}
