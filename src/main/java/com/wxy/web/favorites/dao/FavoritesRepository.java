package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Integer> {

    List<Favorites> findByUserId(Integer userId);

    List<Favorites> findByCategoryId(Integer categoryId);

    /**
     * 查询分类下前40条数据
     *
     * @param categoryId
     * @return
     */
    List<Favorites> findTop40ByCategoryIdOrderBySortDescIdAsc(Integer categoryId);

    /**
     * 模糊搜索
     *
     * @param userId
     * @param name
     * @return
     */
    List<Favorites> findTop100ByUserIdAndNameLikeOrPinyinLike(Integer userId, String name,String pinyin);
}
