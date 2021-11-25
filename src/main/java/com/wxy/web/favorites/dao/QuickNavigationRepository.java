package com.wxy.web.favorites.dao;

import com.wxy.web.favorites.model.QuickNavigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuickNavigationRepository extends JpaRepository<QuickNavigation, Integer>, JpaSpecificationExecutor<QuickNavigation> {

    List<QuickNavigation> findAllByUserId(Integer userId);
}
