package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SearchTypeRepository  extends JpaRepository<SearchType, Integer>, JpaSpecificationExecutor<SearchType> {
    List<SearchType> findByUserId(Integer userId);

    Page<SearchType> findPageByUserId(Integer userId, Pageable pageable);

    int countByUserId(Integer userId);

    void deleteAllByUserId(Integer userId);
}
