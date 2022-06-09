package com.wxy.web.favorites;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.websocket.NioWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableSwagger2
@Slf4j
public class Application {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        Environment environment = context.getBean(Environment.class);
        String path = Optional.ofNullable(environment.getProperty("server.servlet.context-path")).orElse("/");
        log.info("访问地址：http://{}:{}{}", InetAddress.getLocalHost().getHostAddress(),
                environment.getProperty("server.port"), path);
    }

    @Autowired
    private AppConfig appConfig;

    @Value("${netty.port:8889}")
    private Integer nettyPort;

    @Value("${netty.enable:false}")
    private Boolean nettyEnable;

    @PostConstruct
    public void run() throws Exception {
        Path repository = Paths.get(appConfig.getFileRepository());
        if (!Files.exists(repository) && Files.exists(Files.createDirectory(repository))) {
            log.info("创建文件仓库：[{}]", repository.toAbsolutePath());
        }
        // 启动websocket
        if (nettyEnable) {
            new NioWebSocketServer(nettyPort).init();
        }
    }
}

