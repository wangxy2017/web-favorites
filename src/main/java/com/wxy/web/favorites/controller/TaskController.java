package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.TaskService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            task.setLevel(4);
            taskService.save(task);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/all/{date}")
    public ApiResponse findAll(@PathVariable String date) {
        User user = springUtils.getCurrentUser();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
        cal.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<Task> list = taskService.findAllByUserId(date + "-01", date + "-" + lastDay, user.getId());
        // 按每天分组
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<Task>> listMap = list.stream().collect(Collectors.groupingBy(t -> sdf.format(t.getTaskDate())));
        List<Map<String, Object>> dataList = listMap.entrySet().stream().map(entry -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", entry.getKey());
            map.put("count", entry.getValue().size());
            map.put("taskList", entry.getValue());
            return map;
        }).collect(Collectors.toList());
        return ApiResponse.success(dataList);
    }

}
