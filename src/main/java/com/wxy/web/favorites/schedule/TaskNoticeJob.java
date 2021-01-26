package com.wxy.web.favorites.schedule;

import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.TaskService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author wangxiaoyuan
 * 2021/1/25 14:21
 **/
@Component
public class TaskNoticeJob {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtils emailUtils;

    @Scheduled(cron = "0 0,30 6-18 * * ?")
    public void run() {
        // 查询此刻任务
        List<Task> taskList = taskService.findByAlarmTime(new Date(System.currentTimeMillis() / 1000 / 60));
        // 邮件通知
        taskList.stream().filter(t -> t.getLevel() < 4).forEach(t -> {
            User user = userService.findById(t.getUserId());
            emailUtils.send(user.getEmail(), "网络收藏夹|日程通知", t.getContent());
        });
    }
}
