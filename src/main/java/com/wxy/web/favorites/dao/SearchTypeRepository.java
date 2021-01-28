package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.SearchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SearchTypeRepository extends JpaRepository<SearchType, Integer> {
    List<SearchType> findByUserId(Integer userId);
}
