package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.Memorandum;
import com.wxy.web.favorites.model.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MemorandumRepository extends JpaRepository<Memorandum, Integer> {

    Page<Memorandum> findPageByUserIdAndContentLike(Integer userId, String content, Pageable pageable);

    List<Memorandum> findByUserId(Integer userId);

    long countByUserId(Integer userId);

    List<Memorandum> findByUserIdAndContentLike(Integer userId, String content, Sort by);
}
