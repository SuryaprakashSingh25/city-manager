package com.project.notification_service.infrastructure.kafka;

import com.project.notification_service.application.service.NotificationService;
import com.project.event_contracts.shared.event.Issues.IssueCreatedEvent;
import com.project.event_contracts.shared.event.Issues.IssueStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssueEventListener {
    private final NotificationService notificationService;

    @KafkaListener(topics = "issue-create", groupId = "notification-group")
    public void handleIssueCreated(IssueCreatedEvent event){
        notificationService.handleIssueCreated(event);
    }

    @KafkaListener(topics = "issue-status-change", groupId = "notification-group")
    public void handleIssueStatusChanged(IssueStatusChangedEvent event) {
        // Notify creator about status change
        notificationService.handleIssueAssignedOrUpdated(event);
    }

}
