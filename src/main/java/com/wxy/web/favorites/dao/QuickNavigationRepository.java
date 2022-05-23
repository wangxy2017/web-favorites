package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.QuickNavigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuickNavigationRepository extends JpaRepository<QuickNavigation, Integer>, JpaSpecificationExecutor<QuickNavigation> {

    List<QuickNavigation> findAllByUserIdOrderBySort(Integer userId);

    long countByUserId(Integer userId);

    void deleteAllByUserId(Integer userId);

    @Query(nativeQuery = true, value = "select max(sort) from t_quick_navigation where user_id = :userId")
    Integer getSortByUserId(Integer userId);

    @Modifying
    @Query(nativeQuery = true, value = "update t_quick_navigation set sort = :sort where id = :id")
    void updateSort(Integer id, Integer sort);
}
