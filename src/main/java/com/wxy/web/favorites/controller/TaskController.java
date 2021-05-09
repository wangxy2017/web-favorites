package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.TaskService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private SpringUtils springUtils;

    @PostMapping
    public ApiResponse save(@RequestBody Task task) {
        User user = springUtils.getCurrentUser();
        task.setUserId(user.getId());
        if (task.getId() == null) task.setCreateTime(new Date());
        taskService.save(task);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse query(@PathVariable Integer id) {
        Task task = taskService.findById(id);
        return ApiResponse.success(task);
    }

    @GetMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        taskService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/done/{id}")
    public ApiResponse done(@PathVariable Integer id) {
        Task task = taskService.findById(id);
        if (task != null) {
            task.setLevel(PublicConstants.TASK_LEVEL_4);
            taskService.save(task);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/all/{date}")
    public ApiResponse findAll(@PathVariable String date) {
        User user = springUtils.getCurrentUser();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<Task> list = taskService.findAllByUserId(date + "-01", date + "-" + lastDay, user.getId());
        // 按每天分组
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        Map<String, List<Task>> listMap = list.stream().collect(Collectors.groupingBy(t -> sdf.format(t.getTaskDate())));
        List<Map<String, Object>> dataList = listMap.entrySet().stream().map(entry -> {
            List<Task> tasks = entry.getValue();
            Map<Integer, Long> levelCountList = tasks.stream().collect(Collectors.groupingBy(Task::getLevel, Collectors.counting()));
            Map<String, Object> map = new HashMap<>();
            map.put("date", entry.getKey());
            map.put("redTasks", Optional.ofNullable(levelCountList.get(PublicConstants.TASK_LEVEL_0)).orElse(0L));
            map.put("orangeTasks", Optional.ofNullable(levelCountList.get(PublicConstants.TASK_LEVEL_1)).orElse(0L));
            map.put("greenTasks", Optional.ofNullable(levelCountList.get(PublicConstants.TASK_LEVEL_2)).orElse(0L));
            map.put("blueTasks", Optional.ofNullable(levelCountList.get(PublicConstants.TASK_LEVEL_3)).orElse(0L));
            map.put("grayTasks", Optional.ofNullable(levelCountList.get(PublicConstants.TASK_LEVEL_4)).orElse(0L));
            map.put("totalTasks", tasks.size());
            map.put("taskList", tasks);
            return map;
        }).collect(Collectors.toList());
        return ApiResponse.success(dataList);
    }

}
