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
public class TaskNoticeJob {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtils emailUtils;

    @Scheduled(cron = "${cron.task-notice-job}")
    @Transactional(rollbackFor = Exception.class)
    public void run() throws ParseException {
        try {
            log.info("任务通知程序开始执行...");
            // 查询此刻任务
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            List<Task> taskList = taskService.findByAlarmTime(sdf.format(new Date()));
            // 邮件通知
            List<Task> noticeList = taskList.stream().filter(t -> t.getLevel() < PublicConstants.TASK_LEVEL_4).collect(Collectors.toList());
            noticeList.forEach(t -> {
                User user = userService.findById(t.getUserId());
                emailUtils.sendHtmlMail(user.getEmail(), EmailConstants.TASK_NOTICE_TITLE, t.getContent());
            });
        } catch (Exception e) {
            log.error("日程通知任务执行失败：{}", e.getMessage(), e);
            throw new RuntimeException("日程通知任务执行失败");
        }
    }
}
