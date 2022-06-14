package com.wxy.web.favorites.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.DataConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.dao.*;
import com.wxy.web.favorites.model.Category;
import com.wxy.web.favorites.model.Favorites;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.model.UserFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private UserFileRepository userFileRepository;

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
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Date now = new Date();
        if (user.getRegisterTime() == null) {
            user.setRegisterTime(now);
            userRepository.save(user);
        }
        if (user.getLastOnlineTime() == null) {
            user.setLastOnlineTime(now);
            userRepository.save(user);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("clickCount", Optional.ofNullable(user.getClickCount()).orElse(0));
        data.put("searchCount", Optional.ofNullable(user.getSearchCount()).orElse(0));
        data.put("registerDay", DateUtil.between(user.getRegisterTime(), now, DateUnit.DAY) + 1);
        data.put("onlineHour", Optional.ofNullable(user.getOnlineHour()).orElse(0) + 1);
        data.put("categoryCount", categoryRepository.countByUserId(userId));
        data.put("favoriteCount", favoritesRepository.countByUserIdAndDeleteFlag(userId, 0));
        data.put("momentCount", momentRepository.countByUserId(userId));
        data.put("taskCount", taskRepository.countByUserId(userId));
        data.put("navigationCount", quickNavigationRepository.countByUserId(userId));
        data.put("memorandumCount", memorandumRepository.countByUserId(userId));
        data.put("searchTypeCount", searchTypeRepository.countByUserId(userId));
        data.put("fileCount", userFileRepository.countByUserIdAndIsDir(userId,0));
        data.put("shareCount", favoritesRepository.countByUserIdAndIsShare(userId, PublicConstants.SHARE_CODE));
        data.put("recycleCount", favoritesRepository.countByUserIdAndDeleteFlag(userId, PublicConstants.DELETE_CODE));
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
        Category category = new Category().setName(PublicConstants.DEFAULT_CATEGORY_NAME).setUserId(userId).setIsSystem(PublicConstants.SYSTEM_CATEGORY_CODE).setSort(PublicConstants.MAX_SORT_NUMBER).setPinyin(PublicConstants.DEFAULT_CATEGORY_NAME).setPinyinS(PublicConstants.DEFAULT_CATEGORY_NAME);
        categoryRepository.save(category);
        // 推荐收藏
        List<Favorites> favorites = DataConstants.RECOMMEND_LIST.stream().map(dto -> new Favorites().setName(dto.getName()).setIcon(dto.getUrl() + "/favicon.ico").setUrl(dto.getUrl()).setCategoryId(category.getId()).setUserId(userId).setPinyin(dto.getName()).setPinyinS(dto.getName())).collect(Collectors.toList());
        favoritesRepository.saveAll(favorites);
    }

    public void shareCancel(Integer id) {
        UserFile userFile = userFileRepository.findById(id).orElse(null);
        if (userFile != null) {
            userFile.setShareId(null);
            userFileRepository.save(userFile);
        }
    }

    public PageInfo<User> findAdminPageList(String name, Integer pageNum, Integer pageSize) {
        return null;
    }
}
