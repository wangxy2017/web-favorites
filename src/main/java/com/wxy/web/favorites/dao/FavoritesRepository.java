package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Integer> {

    List<Favorites> findByUserId(Integer userId);
}
