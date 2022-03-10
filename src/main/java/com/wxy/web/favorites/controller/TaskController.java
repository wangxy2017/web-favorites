package com.wxy.web.favorites.controller;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.TaskService;
import com.wxy.web.favorites.core.ApiResponse;
import com.wxy.web.favorites.core.PageInfo;
import com.wxy.web.favorites.security.ContextUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
@Api(tags = "日程管理")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ContextUtils contextUtils;

    @PostMapping
    @ApiOperation(value = "新增日程")
    public ApiResponse save(@RequestBody Task task) {
        User user = contextUtils.getCurrentUser();
        task.setUserId(user.getId());
        if (task.getId() == null) task.setCreateTime(new Date());
        taskService.save(task);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询")
    public ApiResponse query(@PathVariable Integer id) {
        Task task = taskService.findById(id);
        return ApiResponse.success(task);
    }

    @GetMapping("/delete/{id}")
    @ApiOperation(value = "删除日程")
    public ApiResponse delete(@PathVariable Integer id) {
        taskService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/clean/{date}")
    @ApiOperation(value = "清空日程")
    public ApiResponse cleanByDate(@PathVariable String date) throws ParseException {
        User user = contextUtils.getCurrentUser();
        taskService.deleteAllByDate(user.getId(), date);
        return ApiResponse.success();
    }

    @GetMapping("/done/{id}")
    @ApiOperation(value = "完成")
    public ApiResponse done(@PathVariable Integer id) {
        Task task = taskService.findById(id);
        if (task != null) {
            task.setLevel(PublicConstants.TASK_LEVEL_4);
            taskService.save(task);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/cancel/{id}")
    @ApiOperation(value = "取消")
    public ApiResponse cancel(@PathVariable Integer id) {
        Task task = taskService.findById(id);
        if (task != null) {
            task.setLevel(PublicConstants.TASK_LEVEL_5);
            taskService.save(task);
            return ApiResponse.success();
        }
        return ApiResponse.error();
    }

    @GetMapping("/all/{date}")
    @ApiOperation(value = "日程统计")
    public ApiResponse findAll(@PathVariable String date) throws ParseException {
        User user = contextUtils.getCurrentUser();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        List<Map<String, Object>> dataList = taskService.countByDayBetween(user.getId(), date + "-01", date + "-" + lastDay);
        return ApiResponse.success(dataList);
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询日程列表")
    public ApiResponse findPageList(@RequestParam String date, @RequestParam Integer pageNum, @RequestParam Integer pageSize) throws ParseException {
        User user = contextUtils.getCurrentUser();
        PageInfo<Task> page = taskService.findPageByUserIdAndTaskDate(user.getId(), date, pageNum, pageSize);
        return ApiResponse.success(page);
    }

}
