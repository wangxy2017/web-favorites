package com.wxy.web.favorites.schedule;

import com.wxy.web.favorites.model.Task;
import com.wxy.web.favorites.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;

/**
 * @author wangxiaoyuan
 * 2021/1/25 15:16
 **/
@Component
public class TaskCancelJob {

    @Autowired
    private TaskService taskService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void run() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, -1);
        // 查询昨日未完成的任务，将状态改为取消
        List<Task> undoTaskList = taskService.findUndoTask(calendar.getTime());
        undoTaskList.forEach(t -> {
            t.setLevel(5);
            taskService.save(t);
        });
    }
}
