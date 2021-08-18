package com.wxy.web.favorites.schedule;

import com.wxy.web.favorites.constant.EmailConstants;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.TaskService;
import com.wxy.web.favorites.service.UserService;
import com.wxy.web.favorites.util.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wangxiaoyuan
 * 2021/1/25 14:21
 **/
@Component
@Slf4j
@Transactional
public class TaskNoticeJob {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtils emailUtils;

    @Scheduled(cron = "${cron.task-notice-job}")
    public void run() throws ParseException {
        log.info("任务通知程序开始执行...");
        // 查询此刻任务
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_MINUTE_PATTERN);
        List<Task> taskList = taskService.findByAlarmTime(sdf.format(new Date()) + ":00");
        // 邮件通知
        List<Task> noticeList = taskList.stream().filter(t -> t.getLevel() < PublicConstants.TASK_LEVEL_4).collect(Collectors.toList());
        noticeList.forEach(t -> {
            User user = userService.findById(t.getUserId());
            emailUtils.sendHtmlMail(user.getEmail(), EmailConstants.TASK_NOTICE_TITLE, t.getContent());
            // 改变状态
            t.setLevel(PublicConstants.TASK_LEVEL_4);
        });
        taskService.saveAll(noticeList);
    }
}
