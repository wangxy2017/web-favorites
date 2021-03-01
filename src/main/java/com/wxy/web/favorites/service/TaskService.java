package com.wxy.web.favorites.service;

import com.wxy.web.favorites.dao.TaskRepository;
import com.wxy.web.favorites.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return taskRepository.findAllByUserId(startDate,endDate,userId);
    }

    public List<Task> findByAlarmTime(String alarmTime) {
        return taskRepository.findByAlarmTime(alarmTime);
    }

    public List<Task> findUndoTask(String taskDate) {
        return taskRepository.findUndoTask(taskDate);
    }
}

