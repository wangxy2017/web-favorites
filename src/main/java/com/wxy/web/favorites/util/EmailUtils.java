package com.wxy.web.favorites.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
@Slf4j
public class EmailUtils {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Async
    public void sendSimpleMail(String mailTo, String mailHead, String mailContent) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(mailTo);
            message.setSubject(mailHead);
            message.setText(mailContent);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败：to = {}, head = {}",mailTo,mailHead,e);
        }
    }

    @Async
    public void sendHtmlMail(String mailTo, String mailHead, String mailContent) {
        try {
            MimeMessage mimeMailMessage = this.javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMailMessage, true, "utf-8");
            messageHelper.setFrom(from);
            messageHelper.setTo(mailTo);
            messageHelper.setSubject(mailHead);
            messageHelper.setText(mailContent, true);
            javaMailSender.send(mimeMailMessage);
        } catch (Exception e) {
            log.error("邮件发送失败：to = {}, head = {}",mailTo,mailHead,e);
        }
    }

}
