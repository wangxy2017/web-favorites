package com.wxy.web.favorites.service;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.TaskRepository;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.util.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        return taskRepository.findAllByUserIdAndTaskDateBetween(userId, sdf.parse(startDate), sdf.parse(endDate));
    }

    public List<Task> findByAlarmTime(String alarmTime) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
        return taskRepository.findByAlarmTime(sdf.parse(alarmTime));
    }

    public List<Task> findUndoTask(String taskDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        List<Integer> levels = Arrays.asList(PublicConstants.TASK_LEVEL_0, PublicConstants.TASK_LEVEL_1, PublicConstants.TASK_LEVEL_2, PublicConstants.TASK_LEVEL_3);
        return taskRepository.findByTaskDateAndLevelIn(sdf.parse(taskDate), levels);
    }

    public PageInfo<Task> findPageByUserIdAndTaskDate(Integer userId, String date, Integer pageNum, Integer pageSize) throws ParseException {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "level"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(orders));
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        Page<Task> page = taskRepository.findAllByUserIdAndTaskDate(userId, sdf.parse(date), pageable);
        return new PageInfo<>(page.getContent(), page.getTotalPages(), page.getTotalElements());
    }

    public List<Map<String, Object>> taskCountByDayBetween(Integer userId, String startDate, String endDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        List<Map<String, Object>> mapList = taskRepository.taskCountByDayBetween(userId, sdf.parse(startDate), sdf.parse(endDate));
        return null;
    }
}

