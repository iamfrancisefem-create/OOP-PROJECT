package com.pms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String SENDGRID_API = "https://api.sendgrid.com/v3/mail/send";

    @Value("${SENDGRID_API_KEY:${MAIL_PASSWORD:}}")
    private String sendgridApiKey;

    @Value("${EMAIL_FROM:projectflow@example.com}")
    private String emailFrom;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper json = new ObjectMapper();

    @Override
    public void sendEmail(String to, String subject, String text) {
        log.warn("=== EMAIL (logged) ===\nTo: {}\nSubject: {}\nBody:\n{}\n=== END ===", to, subject, text);

        if (sendgridApiKey == null || sendgridApiKey.isBlank()) {
            log.warn("SendGrid not configured — set SENDGRID_API_KEY env var. Email not sent to {}", to);
            return;
        }

        new Thread(() -> {
            try {
                Map<String, Object> body = Map.of(
                    "personalizations", new Object[]{ Map.of("to", new Object[]{ Map.of("email", to) }) },
                    "from", Map.of("email", emailFrom),
                    "subject", subject,
                    "content", new Object[]{ Map.of("type", "text/plain", "value", text) }
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SENDGRID_API))
                        .header("Authorization", "Bearer " + sendgridApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                        .timeout(Duration.ofSeconds(15))
                        .build();

                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 202) {
                    log.info("Email sent successfully to {}", to);
                } else {
                    log.error("SendGrid API error ({}): {} — {}", response.statusCode(), to, response.body());
                }
            } catch (Exception e) {
                log.error("Failed to send email to {}: {}", to, e.getMessage());
            }
        }).start();
    }
}
