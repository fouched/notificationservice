package com.example.notificationservice.service;

import com.example.notificationservice.kafka.dto.UserRegistrationEvent;

public interface EmailService {
    void sendWelcomeEmail(UserRegistrationEvent event);
    void sendCreditAlert();
    void sendDebitAlert();
}
