package com.wxy.web.favorites.service;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.TaskRepository;
import com.wxy.web.favorites.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * @Author wangxiaoyuan
 * @Date 2020/4/24 11:50
 * @Description
 **/
@Slf4j
@Service
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
        return taskRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        taskRepository.deleteById(id);
    }

    public List<Task> findAllByUserId(String startDate, String endDate, Integer userId) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        return taskRepository.findAllByUserIdAndTaskDateBetween(userId,sdf.parse(startDate),sdf.parse(endDate));
    }

    public List<Task> findByAlarmTime(String alarmTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
        return taskRepository.findByAlarmTime(sdf.parse(alarmTime));
    }

    public List<Task> findUndoTask(String taskDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        List<Integer> levels = Arrays.asList(PublicConstants.TASK_LEVEL_0, PublicConstants.TASK_LEVEL_1, PublicConstants.TASK_LEVEL_2, PublicConstants.TASK_LEVEL_3);
        return taskRepository.findByTaskDateAndLevelIn(sdf.parse(taskDate),levels);
    }
}

