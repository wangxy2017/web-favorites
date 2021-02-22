package com.wxy.web.favorites.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class StartRunner implements CommandLineRunner {

    @Value("${app.file-repository}")
    private String repository;

    @Override
    public void run(String... args) throws Exception {
        File file = new File(repository);
        if (!file.exists() && file.mkdirs()) {
            log.info("创建文件仓库：[{}]", file.getAbsolutePath());
        }
    }
}
