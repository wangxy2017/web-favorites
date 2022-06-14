package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface PasswordRepository  extends JpaRepository<Password, Integer>, JpaSpecificationExecutor<Password> {

    Password findByFavoritesId(Integer favoritesId);

    void deleteAllByUserId(Integer userId);
}
