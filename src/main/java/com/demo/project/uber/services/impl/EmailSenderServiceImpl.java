package com.demo.project.uber.services.impl;

import com.demo.project.uber.services.EmailSenderService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender javaMailSender;

    @Override
    @Async
    public void sendEmail(String toEmail, String subject, String body) {

        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            log.info("Email sent successfully");
        } catch (Exception e){
            log.info("Email sending failed, {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void sendEmail(String[] toEmail, String subject, String body) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("");
            message.setBcc(toEmail);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            log.info("Email sent successfully");
        } catch (Exception e){
            log.info("Email sending failed, {}", e.getMessage());
        }
    }
}
