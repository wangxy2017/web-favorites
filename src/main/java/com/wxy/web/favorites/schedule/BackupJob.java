package com.wxy.web.favorites.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/***
 * <p>
 * Description: 描述
 * </p>
 * @author wangxiaoyuan
 * 2021年10月09日
 */
@Component
@Slf4j
public class BackupJob {

    @Autowired
    private DataSource dataSource;

    @Value("${backup.zip-file-path}")
    private String zipFilePath;

    @Scheduled(cron = "${cron.backup-job}")
    public void run() {
        try {
            log.info("数据库备份任务开始执行...");
            Connection conn = dataSource.getConnection();
            Statement stat = conn.createStatement();
            stat.execute(String.format("BACKUP TO '%s'", zipFilePath));
            stat.close();
            conn.close();
        } catch (Exception e) {
            log.error("数据库备份失败：{}", e.getMessage(), e);
        }
    }
}
