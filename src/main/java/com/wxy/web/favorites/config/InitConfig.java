package com.wxy.web.favorites.config;

import com.wxy.web.favorites.websocket.NioWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class InitConfig implements CommandLineRunner {

    @Autowired
    private AppConfig appConfig;

    @Value("${netty.port:8888}")
    private Integer nettyPort;

    @Override
    public void run(String... args) throws Exception {
        Path repository = Paths.get(appConfig.getFileRepository());
        if (!Files.exists(repository) && Files.exists(Files.createDirectory(repository))) {
            log.info("创建文件仓库：[{}]", repository.toAbsolutePath());
        }
        // 启动websocket
        new NioWebSocketServer(nettyPort).init();
    }
}
