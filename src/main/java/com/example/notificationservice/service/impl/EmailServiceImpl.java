package com.example.notificationservice.service.impl;

import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.enums.NotificationStatus;
import com.example.notificationservice.enums.NotificationType;
import com.example.notificationservice.kafka.dto.UserRegistrationEvent;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService  {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendWelcomeEmail(UserRegistrationEvent event) {
        try {
            Context context = new Context();
            context.setVariable("firstName", event.getFirstName());
            context.setVariable("lastName", event.getLastName());
            context.setVariable("email", event.getEmail());
            context.setVariable("accountNumber", event.getAccountNumber());
            context.setVariable("bankName", event.getBankName());

            String htmlTemplate = templateEngine.process("welcome-email", context);

            Notification notification = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .type(NotificationType.EMAIL)
                    .subject("Welcome to " + event.getBankName() + " Your account is created!")
                    .message(htmlTemplate)
                    .status(NotificationStatus.SENT)
                    .build();

            sendEmail(event.getEmail(), notification.getSubject(), htmlTemplate);

            notificationRepository.save(notification);
            log.info("Welcome email sent to {}", event.getEmail());

        } catch (Exception e) {
            log.error("Error sending welcome email", e);
            Notification notification = Notification.builder()
                    .recipientEmail(event.getEmail())
                    .type(NotificationType.EMAIL)
                    .subject("Welcome to " + event.getBankName() + " Your account is created!")
                    .message("Failed to send email")
                    .status(NotificationStatus.FAILED)
                    .build();
            notificationRepository.save(notification);

            throw new RuntimeException("Error sending welcome email", e);
        }
    }

    @Override
    public void sendCreditAlert() {
        // TODO
    }

    @Override
    public void sendDebitAlert() {
        // TODO
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }
}
