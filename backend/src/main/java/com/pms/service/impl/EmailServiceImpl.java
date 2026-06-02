package com.pms.service.impl;

import com.pms.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String text) {
        log.warn("=== EMAIL (logged) ===\nTo: {}\nSubject: {}\nBody:\n{}\n=== END ===", to, subject, text);

        if (mailSender == null || mailUsername == null || mailUsername.isBlank() ||
            mailPassword == null || mailPassword.isBlank()) {
            log.warn("SMTP not configured — set SendGrid or Gmail credentials on Railway. Email not sent to {}", to);
            return;
        }

        new Thread(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
                log.info("Email sent successfully to {}", to);
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", to, e.getMessage());
            }
        }).start();
    }
}
