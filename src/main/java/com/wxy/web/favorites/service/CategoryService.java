package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.util.PageInfo;
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
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }

    public Category findByName(String name,Integer userId){
        return  categoryRepository.findByNameAndUserId(name,userId);
    }

    public List<Category> findByUserId(Integer userId) {
        return categoryRepository.findByUserId(userId);
    }

    public List<Category> findCatalog(Integer userId) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "sort"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        return categoryRepository.findByUserId(userId,Sort.by(orders));
    }

    public Category findDefaultCategory(Integer userId) {
        return categoryRepository.findDefaultCategory(userId);
    }

    public PageInfo<Category> findPageByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "sort"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        Page<Category> page = categoryRepository.findByUserId(userId, pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }
}

