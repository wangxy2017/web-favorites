package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.SearchTypeRepository;
import com.wxy.web.favorites.model.SearchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Slf4j
@Service
public class SearchTypeService {

    @Autowired
    private SearchTypeRepository searchTypeRepository;

    public List<SearchType> findByUserId(Integer userId) {
        return searchTypeRepository.findByUserId(userId);
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

