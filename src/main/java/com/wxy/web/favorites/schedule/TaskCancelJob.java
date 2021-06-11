package com.wxy.web.favorites.schedule;

import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @author wangxiaoyuan
 * 2021/1/25 15:16
 **/
@Component
@Slf4j
public class TaskCancelJob {

    @Autowired
    private TaskService taskService;

    @Scheduled(cron = "${cron.task-cancel-job}")
    public void run() {
        log.info("任务取消程序开始执行...");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        // 查询昨日未完成的任务，将状态改为取消
        SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATE_PATTERN);
        List<Task> undoTaskList = taskService.findUndoTask(sdf.format(calendar.getTime()));
        undoTaskList.forEach(t -> t.setLevel(PublicConstants.TASK_LEVEL_5));
        taskService.saveAll(undoTaskList);
    }
}
