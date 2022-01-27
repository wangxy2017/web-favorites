package com.wxy.web.favorites.service;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.*;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.util.PinYinUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private MemorandumRepository memorandumRepository;

    @Autowired
    private QuickNavigationRepository quickNavigationRepository;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AppConfig appConfig;

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

    public User findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    public Map<String, Object> findUserData(Integer userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("favorites", favoritesRepository.countByUserId(userId));
        data.put("moments", momentRepository.countByUserId(userId));
        data.put("tasks", taskRepository.countByUserId(userId));
        data.put("searchTypes", searchTypeRepository.countByUserId(userId));
        data.put("navigations", quickNavigationRepository.countByUserId(userId));
        data.put("memorandums", memorandumRepository.countByUserId(userId));
        return data;
    }

    public void deleteAllData(Integer userId) {
        categoryRepository.deleteAllByUserId(userId);
        favoritesRepository.deleteAllByUserId(userId);
        passwordRepository.deleteAllByUserId(userId);
        momentRepository.deleteAllByUserId(userId);
        taskRepository.deleteAllByUserId(userId);
        searchTypeRepository.deleteAllByUserId(userId);
        quickNavigationRepository.deleteAllByUserId(userId);
        memorandumRepository.deleteAllByUserId(userId);
        initData(userId);
    }

    public void initData(Integer userId) {
        // 创建默认分类
        Category category = new Category(null, PublicConstants.DEFAULT_CATEGORY_NAME, userId, PublicConstants.SYSTEM_CATEGORY_CODE, PublicConstants.MAX_SORT_NUMBER, null, null, null);
        categoryRepository.save(category);
        // 推荐收藏
        List<Favorites> favorites = appConfig.getRecommends().stream().map(s -> {
            String[] split = s.split(PublicConstants.DEFAULT_DELIMITER);
            return new Favorites(null, split[0], split[1] + "favicon.ico"
                    , split[1], category.getId(), userId,
                    PinYinUtils.toPinyin(split[0]),
                    PinYinUtils.toPinyinS(split[0]),
                    null, null, null, null, null, null, null, null, null, null, null);
        }).collect(Collectors.toList());
        favoritesRepository.saveAll(favorites);
    }
}
