package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByUserId(Integer userId);
}
