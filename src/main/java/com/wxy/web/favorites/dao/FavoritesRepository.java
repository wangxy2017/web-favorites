package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Favorites;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface FavoritesRepository extends JpaRepository<Favorites, Integer>, JpaSpecificationExecutor<Favorites> {

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

    void deleteByDeleteFlagAndDeleteTimeBefore(Integer deleteFlag, Date time);

    Long countByUserId(Integer userId);

    Page<Favorites> findByUserIdAndIsShare(Integer userId, Integer shareCode, Pageable pageable);

    @Query("select new Favorites (f.id,f.name,f.icon,f.url,f.support,u.username) from Favorites f left join User u on u.id = f.userId where f.isShare = 1 and f.deleteFlag is null and ( f.name like :name or f.pinyin like :name or f.pinyinS like :name)")
    Page<Favorites> findShareList(String name, Pageable pageable);

    Favorites findByUserIdAndUrl(Integer userId, String url);

    void deleteAllByUserId(Integer userId);

    Long countByUserIdAndDeleteFlagIsNull(Integer userId);

    Long countByUserIdAndDeleteFlag(Integer userId, Integer deleteCode);

    Long countByUserIdAndIsShare(Integer userId, Integer shareCode);
}
