package com.wxy.web.favorites.service;

import cn.hutool.core.util.StrUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.ErrorConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.dao.CategoryRepository;
import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.util.JpaUtils;
import com.wxy.web.favorites.util.SqlUtils;
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
import java.util.Optional;

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
        return favoritesRepository.findByCategoryIdAndDeleteFlag(categoryId, 0);
    }

    public Favorites save(Favorites favorites) {
        if (favorites.getId() != null) {
            favoritesRepository.findById(favorites.getId()).ifPresent(source -> JpaUtils.copyNotNullProperties(source, favorites));
        }
        return favoritesRepository.save(favorites);
    }

    public List<Favorites> saveAll(List<Favorites> list) {
        list.forEach(favorites -> {
            if (favorites.getId() != null) {
                favoritesRepository.findById(favorites.getId()).ifPresent(source -> JpaUtils.copyNotNullProperties(source, favorites));
            }
        });
        return favoritesRepository.saveAll(list);
    }

    public List<Favorites> findLimitByCategoryId(Integer categoryId) {
        Sort sort = Sort.by(Sort.Order.desc("sort"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(0, appConfig.getFavoritesLimit(), sort);
        return favoritesRepository.findLimitByCategoryIdAndDeleteFlag(categoryId, 0, pageable);
    }

    public void deleteById(Integer id) {
        favoritesRepository.deleteById(id);
    }

    public Favorites findById(Integer id) {
        return favoritesRepository.findById(id).orElse(null);
    }

    public Favorites findByShortcut(String shortcut, Integer userId) {
        return favoritesRepository.findByShortcutAndUserIdAndDeleteFlag(shortcut, userId, 0);
    }

    public List<Favorites> findFavorites(Integer userId, String searchName) {
        String text = SqlUtils.trimAndEscape(searchName);
        Pageable pageable = PageRequest.of(0, appConfig.getFavoritesSearchLimit());
        // 构造自定义查询条件
        Specification<Favorites> queryCondition = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();
            predicateList.add(criteriaBuilder.equal(root.get("userId"), userId));
            predicateList.add(criteriaBuilder.equal(root.get("deleteFlag"), 0));
            if (StrUtil.isNotBlank(text)) {
                predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("name"), "%" + text + "%"), criteriaBuilder.like(root.get("pinyin"), "%" + text + "%"), criteriaBuilder.like(root.get("pinyinS"), "%" + text + "%")));
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

    public PageInfo<Favorites> findShareByPage(Integer userId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Favorites> page = favoritesRepository.findByUserIdAndIsShare(userId, PublicConstants.SHARE_CODE, pageable);
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

    public void updateShare(Integer id) {
        Favorites favorites = favoritesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(ErrorConstants.ILLEGAL_OPERATION_MSG));
        favorites.setIsShare(null);
        favoritesRepository.save(favorites);
    }

    public PageInfo<Favorites> findShareList(String name, Integer pageNum, Integer pageSize) {
        name = Optional.ofNullable(name).orElse("").toLowerCase();
        name = SqlUtils.trimAndEscape(name);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "clickCount"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        Page<Favorites> page = favoritesRepository.findShareList("%" + name + "%", pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());

    }

    public boolean saveSupport(Integer userId, Integer id) {
        Category defaultCategory = categoryRepository.findDefaultCategory(userId);
        Favorites favorites = favoritesRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(ErrorConstants.ILLEGAL_OPERATION_MSG));
        Favorites favorites1 = favoritesRepository.findByUserIdAndUrl(userId, favorites.getUrl());
        if (favorites1 == null || PublicConstants.DELETE_CODE.equals(favorites1.getDeleteFlag())) {
            Favorites favorites2 = new Favorites();
            favorites2.setName(favorites.getName());
            favorites2.setIcon(favorites.getIcon());
            favorites2.setUrl(favorites.getUrl());
            favorites2.setCategoryId(defaultCategory.getId());
            favorites2.setUserId(userId);
            favorites2.setPinyin(favorites.getPinyin());
            favorites2.setPinyinS(favorites.getPinyinS());
            favoritesRepository.save(favorites2);
            // support + 1
            favorites.setSupport(Optional.ofNullable(favorites.getSupport()).orElse(0) + 1);
            favoritesRepository.save(favorites);
            return true;
        }
        return false;
    }
}
