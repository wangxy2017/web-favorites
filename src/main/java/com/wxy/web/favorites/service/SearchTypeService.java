package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.SearchTypeRepository;
import com.wxy.web.favorites.model.Moment;
import com.wxy.web.favorites.model.SearchType;
import com.wxy.web.favorites.util.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Slf4j
@Service
@Transactional
public class SearchTypeService {

    @Autowired
    private SearchTypeRepository searchTypeRepository;

    public List<SearchType> findByUserId(Integer userId) {
        return searchTypeRepository.findByUserId(userId);
    }

    public PageInfo<SearchType> findPageByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<SearchType> page = searchTypeRepository.findPageByUserId(userId, pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    public SearchType save(SearchType searchType) {
        return searchTypeRepository.save(searchType);
    }

    public SearchType findById(Integer id) {
        return searchTypeRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        searchTypeRepository.deleteById(id);
    }

    public void saveAll(List<SearchType> searchTypeList) {
        searchTypeRepository.saveAll(searchTypeList);
    }
}

