package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.UserFileRepository;
import com.wxy.web.favorites.model.UserFile;
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
public class UserFileService {

    @Autowired
    private UserFileRepository userFileRepository;

    public UserFile save(UserFile userFile) {
        return userFileRepository.save(userFile);
    }

    public UserFile findById(Integer id) {
        return userFileRepository.getOne(id);
    }

    public List<UserFile> findByPid(Integer pid) {
        return userFileRepository.findByPid(pid);
    }

    public List<UserFile> findRootList(Integer userId) {
        return userFileRepository.findByUserIdAndPidIsNull(userId);
    }

    public List<UserFile> searchFiles(String filename){
        return userFileRepository.findByUserIdAndFilenameLike(filename);
    }

    public void deleteById(Integer id) {
        userFileRepository.deleteById(id);
    }
}

