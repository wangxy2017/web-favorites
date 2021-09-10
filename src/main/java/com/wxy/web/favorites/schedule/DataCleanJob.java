package com.wxy.web.favorites.schedule;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.constant.PublicConstants;
import com.wxy.web.favorites.service.VerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Component
@Slf4j
@Transactional
public class DataCleanJob {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private AppConfig appConfig;

    /**
     * 清理大于30天的数据
     */
    @Scheduled(cron = "${cron.data-clean-job}")
    public void run() {
        try {
            log.info("验证码清理任务开始执行...");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -appConfig.getVerificationExpiredMinutes());
            SimpleDateFormat sdf = new SimpleDateFormat(PublicConstants.FORMAT_DATETIME_PATTERN);
            verificationService.deleteBeforeTime(sdf.format(calendar.getTime()));
        } catch (Exception e) {
            log.error("验证码清理任务执行失败：{}", e.getMessage(), e);
        }
    }
}
