package com.wxy.web.favorites.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class StartRunner implements CommandLineRunner {

    @Autowired
    private AppConfig appConfig;

    @Override
    public void run(String... args) throws Exception {
        File file = new File(appConfig.getFileRepository());
        if (!file.exists() && file.mkdirs()) {
            log.info("创建文件仓库：[{}]", file.getAbsolutePath());
        }
    }
}
