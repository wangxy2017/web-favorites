package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.UserFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Integer>, JpaSpecificationExecutor<Favorites> {

    List<Favorites> findByUserIdAndDeleteFlagIsNull(Integer userId);

    List<Favorites> findByCategoryIdAndDeleteFlagIsNull(Integer categoryId);

    /**
     * 查询分类下前40条数据
     *
     * @param categoryId
     * @return
     */
    List<Favorites> findLimitByCategoryIdAndDeleteFlagIsNull(Integer categoryId, Pageable pageable);

    @Query("select f from  Favorites f where f.userId = :userId and f.star = 1 and f.deleteFlag is null order by f.visitTime desc")
    List<Favorites> findStarFavorites(Integer userId);

    Favorites findByShortcutAndUserIdAndDeleteFlagIsNull(String shortcut, Integer userId);

    Page<Favorites> findByUserIdAndDeleteFlag(Integer userId, Integer deleteFlag, Pageable pageable);

    @Query("delete from Favorites f where f.deleteFlag = 1 and f.deleteTime < :time")
    @Modifying
    void cleanRecycleBeforeTime(String time);
}
