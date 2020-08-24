package com.wxy.web.favorites.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 异步发送邮件
     *
     * @param to    收件人
     * @param title 主题
     * @param text  内容
     */
    @Async
    public void send(String to, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(text);
        message.setTo(to);
        message.setFrom(from);
        javaMailSender.send(message);
    }
}
