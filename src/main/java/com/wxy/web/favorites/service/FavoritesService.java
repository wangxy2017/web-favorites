package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Favorites;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class FavoritesService {

    @Value("${favorites.limit:40}")
    private int favoritesLimit;

    @Value("${search.limit:100}")
    private int searchLimit;

    @Autowired
    private FavoritesRepository favoritesRepository;

    public void deleteAll(List<Favorites> list) {
        favoritesRepository.deleteAll(list);
    }

    public List<Favorites> findByCategoryId(Integer categoryId) {
        return favoritesRepository.findByCategoryId(categoryId);
    }

    public Favorites save(Favorites favorites) {
        return favoritesRepository.save(favorites);
    }

    public List<Favorites> saveAll(List<Favorites> list) {
        return favoritesRepository.saveAll(list);
    }

    public List<Favorites> findLimitByCategoryId(Integer categoryId) {
        Sort sort = Sort.by(Sort.Order.desc("sort"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, favoritesLimit, sort);
        return favoritesRepository.findLimitByCategoryId(categoryId, pageable);
    }

    public void deleteById(Integer id) {
        favoritesRepository.deleteById(id);
    }

    public Favorites findById(Integer id) {
        return favoritesRepository.getOne(id);
    }

    public List<Favorites> searchFavorites(Integer userId, String searchName) {
        Pageable pageable = PageRequest.of(0, searchLimit);
        // 构造自定义查询条件
        Specification<Favorites> queryCondition = (Specification<Favorites>) (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(criteriaBuilder.equal(root.get("userId"), userId));
            if (StringUtils.isNotBlank(searchName)) {
                predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("name"), "%" + searchName + "%"), criteriaBuilder.like(root.get("pinyin"), "%" + searchName + "%")));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
        return favoritesRepository.findAll(queryCondition, pageable).getContent();
    }

    public List<Favorites> findStarFavorites(Integer userId) {
        return favoritesRepository.findStarFavorites(userId);
    }
}
