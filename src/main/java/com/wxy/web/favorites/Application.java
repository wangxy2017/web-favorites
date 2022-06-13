package com.wxy.web.favorites;

import com.wxy.web.favorites.config.AppConfig;
import com.wxy.web.favorites.model.User;
import com.wxy.web.favorites.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
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

    @Value("${netty.port}")
    private Integer nettyPort;

    @Value("${netty.enable}")
    private Boolean nettyEnable;

    @Value("${init.user.username}")
    private String username;

    @Value("${init.user.password}")
    private String password;

    @Value("${init.admin.username}")
    private String adminUsername;

    @Value("${init.admin.password}")
    private String adminPassword;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        // 初始化用户账号
        if (userService.findByUsername(username) == null) {
            User user = userService.save(new User().setUsername(username).setPassword(passwordEncoder.encode(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)))));
            log.info("初始化用户账号：{}", user);
            userService.initData(user.getId());
        }
        // 初始化管理员账号
        if (userService.findByUsername(adminUsername) == null) {
            User user = userService.save(new User().setUsername(adminUsername).setPassword(passwordEncoder.encode(DigestUtils.md5DigestAsHex(adminPassword.getBytes(StandardCharsets.UTF_8)))).setAdmin(1));
            log.info("初始化管理员账号：{}", user);
        }
    }
}

