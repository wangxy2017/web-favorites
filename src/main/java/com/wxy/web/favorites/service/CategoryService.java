package com.wxy.web.favorites.service;

import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.util.SqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class CategoryService {

    @Autowired
    private AppConfig appConfig;

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

    public Category findByName(String name, Integer userId) {
        return categoryRepository.findByNameAndUserId(name, userId);
    }

    public List<Category> findByUserId(Integer userId) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "sort"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        return categoryRepository.findByUserId(userId, Sort.by(orders));
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

    public List<Category> findCategories(Integer userId, String searchName) {
        String text = SqlUtils.trimAndEscape(searchName);
        Pageable pageable = PageRequest.of(0, appConfig.getCategorySearchLimit());
        // 构造自定义查询条件
        Specification<Category> queryCondition = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(criteriaBuilder.equal(root.get("userId"), userId));
            if (StrUtil.isNotBlank(text)) {
                predicateList.add(criteriaBuilder.like(root.get("name"), "%" + text + "%"));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
        return categoryRepository.findAll(queryCondition, pageable).getContent();
    }
}

