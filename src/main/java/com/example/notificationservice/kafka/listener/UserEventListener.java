package com.example.notificationservice.kafka.listener;

import com.example.notificationservice.kafka.dto.UserRegistrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventListener {

    // topics
    private static final String USER_REGISTRATION_TOPIC = "user-registration-topic";

    @KafkaListener(topics = USER_REGISTRATION_TOPIC)
    public void onUserRegistration(UserRegistrationEvent userRegistrationEvent) {
        log.info("Received user registration event for email: {}", userRegistrationEvent.getEmail());
        // TODO: send welcome notification
    }
}
