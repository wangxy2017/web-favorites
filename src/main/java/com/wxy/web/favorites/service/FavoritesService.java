package com.wxy.web.favorites.service;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.util.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public void deleteAll(List<Favorites> list) {
        favoritesRepository.deleteAll(list);
    }

    public List<Favorites> findByCategoryId(Integer categoryId) {
        return favoritesRepository.findByCategoryIdAndDeleteFlagIsNull(categoryId);
    }

    public Favorites save(Favorites favorites) {
        return favoritesRepository.save(favorites);
    }

    public List<Favorites> saveAll(List<Favorites> list) {
        return favoritesRepository.saveAll(list);
    }

    public List<Favorites> findLimitByCategoryId(Integer categoryId) {
        Sort sort = Sort.by(Sort.Order.desc("sort"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, appConfig.getFavoritesLimit(), sort);
        return favoritesRepository.findLimitByCategoryIdAndDeleteFlagIsNull(categoryId, pageable);
    }

    public void deleteById(Integer id) {
        favoritesRepository.deleteById(id);
    }

    public Favorites findById(Integer id) {
        return favoritesRepository.findById(id).orElse(null);
    }

    public Favorites findByShortcut(String shortcut, Integer userId) {
        return favoritesRepository.findByShortcutAndUserIdAndDeleteFlagIsNull(shortcut, userId);
    }

    public List<Favorites> findFavorites(Integer userId, String searchName) {
        Pageable pageable = PageRequest.of(0, appConfig.getFavoritesSearchLimit());
        // 构造自定义查询条件
        Specification<Favorites> queryCondition = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(criteriaBuilder.equal(root.get("userId"), userId));
            predicateList.add(criteriaBuilder.isNull(root.get("deleteFlag")));
            if (StringUtils.isNotBlank(searchName)) {
                predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("name"), "%" + searchName + "%"), criteriaBuilder.like(root.get("pinyin"), "%" + searchName + "%"), criteriaBuilder.like(root.get("pinyinS"), "%" + searchName + "%")));
            }
            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
        return favoritesRepository.findAll(queryCondition, pageable).getContent();
    }

    public List<Favorites> findStarFavorites(Integer userId) {
        return favoritesRepository.findStarFavorites(userId);
    }

    public PageInfo<Favorites> findRecycleByPage(Integer userId, Integer pageNum, Integer pageSize) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "deleteTime"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        Page<Favorites> page = favoritesRepository.findByUserIdAndDeleteFlag(userId, PublicConstants.DELETE_CODE, pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    public void deleteAllFromRecycle(Integer userId) {
        Favorites favorites = new Favorites();
        favorites.setUserId(userId);
        favorites.setDeleteFlag(1);
        List<Favorites> all = favoritesRepository.findAll(Example.of(favorites));
        favoritesRepository.deleteAll(all);
    }

    public void updateDeleteFlag(Integer id, Integer userId) {
        Favorites favorites = favoritesRepository.findById(id).orElse(null);
        if (favorites != null) {
            Category category = categoryRepository.findById(favorites.getCategoryId()).orElse(null);
            if (category == null) {
                category = categoryRepository.findDefaultCategory(userId);
            }
            favorites.setCategoryId(category.getId());
            favorites.setDeleteFlag(null);
            favorites.setDeleteTime(null);
            favoritesRepository.save(favorites);
        }
    }


    public void deleteAllFromRecycleWithBeforeTime(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
        favoritesRepository.deleteByDeleteFlagAndDeleteTimeBefore(PublicConstants.DELETE_CODE, sdf.parse(time));
    }

    public void noShare(Integer id, Integer userId) {

    }
}
