package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.*;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private SearchTypeRepository searchTypeRepository;

    @Autowired
    private TaskRepository taskRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByUsernameAndEmail(String username, String email) {
        return userRepository.findByUsernameAndEmail(username, email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(Integer id){
        return userRepository.findById(id).orElse(null);
    }

    public Map<String, Object> getUserData(Integer userId) {
        Map<String,Object> data = new HashMap<>();
        data.put("favorites",favoritesRepository.countByUserId(userId));
        data.put("moments",momentRepository.countByUserId(userId));
        data.put("tasks",taskRepository.countByUserId(userId));
        data.put("searchTypes",searchTypeRepository.countByUserId(userId));
        return data;
    }
}
