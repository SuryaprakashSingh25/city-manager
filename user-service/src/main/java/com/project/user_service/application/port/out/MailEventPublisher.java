package com.project.user_service.application.port.out;

import com.project.user_service.domain.model.User;

public interface MailEventPublisher {
    void publishUserRegistered(User user);
    void publishPasswordResetRequested(User user);
}
