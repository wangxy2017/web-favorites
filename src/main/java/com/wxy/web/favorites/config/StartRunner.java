package com.wxy.web.favorites.config;

import com.wxy.web.favorites.websocket.NioWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class StartRunner implements CommandLineRunner {

    @Autowired
    private AppConfig appConfig;

    @Value("${netty.port:8888}")
    private Integer nettyPort;

    @Override
    public void run(String... args) throws Exception {
        File file = new File(appConfig.getFileRepository());
        if (!file.exists() && file.mkdirs()) {
            log.info("创建文件仓库：[{}]", file.getAbsolutePath());
        }
        // 启动websocket
        new NioWebSocketServer(nettyPort).init();
    }
}
