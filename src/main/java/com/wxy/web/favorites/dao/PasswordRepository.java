package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PasswordRepository extends JpaRepository<Password, Integer> {

    Password findByFavoritesId(Integer favoritesId);
}
