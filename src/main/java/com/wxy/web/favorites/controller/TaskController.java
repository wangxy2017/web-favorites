package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.TaskService;
import com.wxy.web.favorites.util.ApiResponse;
import com.wxy.web.favorites.util.PageInfo;
import com.wxy.web.favorites.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/clean/{date}")
    public ApiResponse cleanByDate(@PathVariable String date) throws ParseException {
        User user = springUtils.getCurrentUser();
        taskService.cleanByDate(user.getId(), date);
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
    public ApiResponse findAll(@PathVariable String date) throws ParseException {
        User user = springUtils.getCurrentUser();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<Map<String, Object>> dataList = taskService.countByDayBetween(user.getId(), date + "-01", date + "-" + lastDay);
        return ApiResponse.success(dataList);
    }

    @GetMapping("/list")
    public ApiResponse findPageList(@RequestParam String date, @RequestParam Integer pageNum, @RequestParam Integer pageSize) throws ParseException {
        User user = springUtils.getCurrentUser();
        PageInfo<Task> page = taskService.findPageByUserIdAndTaskDate(user.getId(), date, pageNum, pageSize);
        return ApiResponse.success(page);
    }

}
