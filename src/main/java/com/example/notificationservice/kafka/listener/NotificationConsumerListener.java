package com.example.notificationservice.kafka.listener;

import com.example.notificationservice.kafka.dto.UserRegistrationEvent;
import com.example.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumerListener {

    private final EmailService emailService;

    // topics
    private static final String USER_REGISTRATION_TOPIC = "user-registration-topic";

    @KafkaListener(topics = USER_REGISTRATION_TOPIC, groupId = "notification-group")
    public void consumeUserRegistrationEvent(UserRegistrationEvent event) {
        log.info("Received user registration event: {}", event);
        try {
            emailService.sendWelcomeEmail(event);
        } catch (Exception e) {
            log.error("Error processing user registration event: {}", event, e);
        }
    }
}
