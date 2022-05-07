package com.wxy.web.favorites.service;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.dao.TaskRepository;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.util.JpaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        taskList.forEach(task -> {
            if (task.getId() != null) {
                taskRepository.findById(task.getId()).ifPresent(source -> JpaUtils.copyNotNullProperties(source, task));
            }
        });
        taskRepository.saveAll(taskList);
    }

    public Task save(Task task) {
        if (task.getId() != null) {
            taskRepository.findById(task.getId()).ifPresent(source -> JpaUtils.copyNotNullProperties(source, task));
        }
        return taskRepository.save(task);
    }

    public Task findById(Integer id) {
        return taskRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        taskRepository.deleteById(id);
    }

    public void deleteAllByDate(Integer userId, String taskDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        taskRepository.deleteByUserIdAndTaskDate(userId,sdf.parse(taskDate));
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

    public List<Map<String, Object>> countByDayBetween(Integer userId, String startDate, String endDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        List<Map<String, Object>> mapList = taskRepository.taskCountByDayBetween(userId, sdf.parse(startDate), sdf.parse(endDate));
        Map<String, List<Map<String, Object>>> groupList = mapList.stream().collect(Collectors.groupingBy(map -> sdf.format(map.get("taskDate"))));
        return groupList.entrySet().stream().map(entry -> {
            int redTasks = 0;
            int orangeTasks = 0;
            int greenTasks = 0;
            int blueTasks = 0;
            int grayTasks = 0;
            int cancelTasks = 0;
            int totalTasks = 0;
            for (Map<String, Object> map : entry.getValue()) {
                int level = Integer.parseInt(map.get("level").toString());
                int count = Integer.parseInt(map.get("count").toString());
                if (PublicConstants.TASK_LEVEL_0.equals(level)) {
                    redTasks = count;
                } else if (PublicConstants.TASK_LEVEL_1.equals(level)) {
                    orangeTasks = count;
                } else if (PublicConstants.TASK_LEVEL_2.equals(level)) {
                    greenTasks = count;
                } else if (PublicConstants.TASK_LEVEL_3.equals(level)) {
                    blueTasks = count;
                } else if (PublicConstants.TASK_LEVEL_4.equals(level)) {
                    grayTasks = count;
                } else if (PublicConstants.TASK_LEVEL_5.equals(level)) {
                    cancelTasks = count;
                }
                totalTasks += count;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("date", entry.getKey());
            map.put("redTasks", redTasks);
            map.put("orangeTasks", orangeTasks);
            map.put("greenTasks", greenTasks);
            map.put("blueTasks", blueTasks);
            map.put("grayTasks", grayTasks);
            map.put("cancelTasks", cancelTasks);
            map.put("totalTasks", totalTasks);
            return map;
        }).collect(Collectors.toList());
    }

    public List<Task> findUndoTaskByUserId(Integer userId) {
        List<Integer> levels = Arrays.asList(PublicConstants.TASK_LEVEL_0, PublicConstants.TASK_LEVEL_1, PublicConstants.TASK_LEVEL_2, PublicConstants.TASK_LEVEL_3);
        return taskRepository.findByUserIdAndLevelIn(userId,levels);
    }
}

