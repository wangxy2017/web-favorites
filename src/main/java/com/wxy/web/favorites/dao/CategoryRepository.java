package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {

    /**
     * 查询用户下所有分类
     *
     * @param userId
     * @return
     */
    List<Category> findByUserId(Integer userId);

    Category findByNameAndUserId(String name,Integer userId);

    /**
     * 查询用户下所有分类（排序）
     *
     * @param userId
     * @return
     */
    List<Category> findByUserId(Integer userId, Sort sort);

    /**
     * 分页查询
     *
     * @param userId
     * @param pageable 分页信息
     * @return
     */
    Page<Category> findByUserId(Integer userId, Pageable pageable);

    @Query("select c from  Category c where c.userId = :userId and c.isSystem = 1 ")
    Category findDefaultCategory(Integer userId);

    void deleteAllByUserId(Integer userId);
}
