package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.FavoritesRepository;
import com.wxy.web.favorites.model.Favorites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Favorites> findTop40ByCategoryIdOrderBySortDescIdAsc(Integer categoryId) {
        return favoritesRepository.findTop40ByCategoryIdOrderBySortDescIdAsc(categoryId);
    }

    public void deleteById(Integer id) {
        favoritesRepository.deleteById(id);
    }

    public Favorites findById(Integer id) {
        return favoritesRepository.getOne(id);
    }

    public List<Favorites> findTop100ByUserIdAndNameLikeOrPinyinLike(Integer userId, String name, String pinyin) {
        return favoritesRepository.findTop100ByUserIdAndNameLikeOrPinyinLike(userId, name, pinyin);
    }

    public List<Favorites> findStarFavorites(Integer userId){
        return favoritesRepository.findStarFavorites(userId);
    }
}
