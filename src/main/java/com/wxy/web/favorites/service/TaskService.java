package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.TaskRepository;
import com.wxy.web.favorites.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Slf4j
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public void saveAll(List<Task> taskList) {
        taskRepository.saveAll(taskList);
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public Task findById(Integer id) {
        return taskRepository.getOne(id);
    }

    public void deleteById(Integer id) {
        taskRepository.deleteById(id);
    }

    public List<Task> findAllByUserId(String startDate, String endDate, Integer userId) {
        List<Task> list = new ArrayList<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            list = taskRepository.findAllByUserId(start, end, userId);
        } catch (Exception e) {
            log.error("查询失败,startDate:{},endDate:{},userId:{}", startDate, endDate, userId, e);
        }
        return list;
    }

}

