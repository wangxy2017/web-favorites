package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Favorites;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Integer>, JpaSpecificationExecutor<Favorites> {

    List<Favorites> findByUserId(Integer userId);

    List<Favorites> findByCategoryId(Integer categoryId);

    /**
     * 查询分类下前40条数据
     *
     * @param categoryId
     * @return
     */
    List<Favorites> findLimitByCategoryId(Integer categoryId, Pageable pageable);

    @Query("select f from  Favorites f where f.userId = :userId and f.star = 1 order by f.visitTime desc")
    List<Favorites> findStarFavorites(Integer userId);
}
