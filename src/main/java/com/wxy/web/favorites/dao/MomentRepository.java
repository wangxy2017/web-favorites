package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MomentRepository extends JpaRepository<Moment, Integer> {
    /**
     * 分页查询
     *
     * @param userId
     * @param pageable 分页信息
     * @return
     */
    Page<Moment> findPageByUserId(Integer userId, Pageable pageable);

    @Query("select m from  Moment m where m.userId = :userId and m.isTop = 1 ")
    Moment findTopMoment(Integer userId);

    /**
     * 计数
     * @param userId
     * @return
     */
    int countByUserId(Integer userId);

    List<Moment> findByUserId(Integer userId);
}