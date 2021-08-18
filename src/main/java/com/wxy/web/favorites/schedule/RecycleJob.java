package com.wxy.web.favorites.schedule;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.service.FavoritesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component
@Slf4j
@Transactional
public class RecycleJob {

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private AppConfig appConfig;

    /**
     * 清理大于30天的数据
     */
    @Scheduled(cron = "${cron.recycle-job}")
    public void run() {
        try {
            log.info("回收站清理任务开始执行...");
            if (appConfig.getRecycleSaveDays() != null && appConfig.getRecycleSaveDays() > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, -appConfig.getRecycleSaveDays());
                SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);

                favoritesService.cleanRecycleBeforeTime(sdf.format(calendar.getTime()));

            }
        } catch (Exception e) {
            log.error("回收站清理任务执行失败：{}", e.getMessage(), e);
        }
    }
}
