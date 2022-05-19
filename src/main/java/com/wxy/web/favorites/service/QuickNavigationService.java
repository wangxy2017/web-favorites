package com.wxy.web.favorites.service;

import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.core.SortDto;
import com.wxy.web.favorites.dao.QuickNavigationRepository;
import com.wxy.web.favorites.model.QuickNavigation;
import com.wxy.web.favorites.util.JpaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class QuickNavigationService {

    @Autowired
    private QuickNavigationRepository quickNavigationRepository;

    public QuickNavigation save(QuickNavigation quickNavigation) {
        if (quickNavigation.getId() != null) {
            quickNavigationRepository.findById(quickNavigation.getId()).ifPresent(source -> JpaUtils.copyNotNullProperties(source, quickNavigation));
        }
        return quickNavigationRepository.save(quickNavigation);
    }

    public List<QuickNavigation> findByUserId(Integer userId) {
        return quickNavigationRepository.findAllByUserIdOrderBySort(userId);
    }

    public void deleteById(Integer id) {
        quickNavigationRepository.deleteById(id);
    }

    public int getSortByUserId(Integer userId) {
        Integer sort = quickNavigationRepository.getSortByUserId(userId);
        return Optional.ofNullable(sort).orElse(0) + 1;
    }

    public void sort(SortDto dto) {
        QuickNavigation first = quickNavigationRepository.findById(dto.getFirstId()).orElseThrow(() -> new IllegalArgumentException(ErrorConstants.ILLEGAL_OPERATION_MSG));
        QuickNavigation second = quickNavigationRepository.findById(dto.getSecondId()).orElseThrow(() -> new IllegalArgumentException(ErrorConstants.ILLEGAL_OPERATION_MSG));
        if (first.getSort() == null) {
            first.setSort(getSortByUserId(first.getUserId()));
            quickNavigationRepository.save(first);
        }
        if (second.getSort() == null) {
            second.setSort(getSortByUserId(second.getUserId()));
            quickNavigationRepository.save(second);
        }
        Integer temp = first.getSort();
        first.setSort(second.getSort());
        quickNavigationRepository.save(first);
        second.setSort(temp);
        quickNavigationRepository.save(second);
    }
}

