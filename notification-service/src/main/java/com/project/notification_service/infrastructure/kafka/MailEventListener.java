package com.project.notification_service.infrastructure.kafka;

import com.project.event_contracts.shared.event.PasswordResetRequestedEvent;
import com.project.event_contracts.shared.event.UserRegisteredEvent;
import com.project.notification_service.application.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailEventListener {

    private final MailService mailService;

    @KafkaListener(topics = "user-registered", groupId = "notification-group")
    public void handleUserRegistered(UserRegisteredEvent event){
        mailService.sendVerificationEmail(event.getEmail(), event.getVerificationToken());
    }

    @KafkaListener(topics = "password-reset", groupId = "notification-group")
    public void handlePasswordReset(PasswordResetRequestedEvent event){
        mailService.sendPasswordResetEmail(event.getEmail(), event.getResetToken());
    }

}
