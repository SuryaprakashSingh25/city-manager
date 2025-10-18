package com.project.user_service.infrastructure.kafka;

import com.project.event_contracts.shared.event.PasswordResetRequestedEvent;
import com.project.event_contracts.shared.event.UserRegisteredEvent;
import com.project.user_service.application.port.out.MailEventPublisher;
import com.project.user_service.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMailEventPublisher implements MailEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_REGISTERED_TOPIC="user-registered";
    private static final String PASSWORD_RESET_TOPIC="password-reset";

    @Override
    public void publishUserRegistered(User user) {
        UserRegisteredEvent event=new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getVerificationToken()
        );
        kafkaTemplate.send(USER_REGISTERED_TOPIC,user.getId(),event);
    }

    @Override
    public void publishPasswordResetRequested(User user) {
        PasswordResetRequestedEvent event=new PasswordResetRequestedEvent(
                user.getId(),
                user.getEmail(),
                user.getResetToken()
        );
        kafkaTemplate.send(PASSWORD_RESET_TOPIC,user.getId(),event);
    }
}
