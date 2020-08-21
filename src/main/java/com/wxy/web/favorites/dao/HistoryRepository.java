package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {


    @Query("select f from History h left join Favorites f on f.id = h.favoritesId where h.userId = :userId order by h.id desc")
    List<Favorites> findHistoryFavorites(Integer userId);

    void deleteByFavoritesId(Integer favoritesId);

    void deleteByUserId(Integer userId);

    History findByFavoritesId(Integer favoritesId);
}
