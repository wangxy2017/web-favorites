package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.HistoryRepository;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    public History save(History history) {
        // 默认保存10个历史记录，且不重复保存
        History history1 = historyRepository.findByFavoritesId(history.getFavoritesId());
        if (history1 != null) {
            historyRepository.deleteById(history1.getId());
        }
        List<Favorites> list = findHistoryFavorites(history.getUserId());
        if (list.size() == 10) {
            historyRepository.deleteByFavoritesId(list.get(list.size() - 1).getId());
        }
        return historyRepository.save(history);
    }

    public List<Favorites> findHistoryFavorites(Integer userId) {
        List<Favorites> list = historyRepository.findHistoryFavorites(userId);
        list = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return list;
    }

    public void deleteByUserId(Integer userId) {
        historyRepository.deleteByUserId(userId);
    }
}

