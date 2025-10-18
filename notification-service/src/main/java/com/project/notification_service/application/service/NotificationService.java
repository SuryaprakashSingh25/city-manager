package com.project.notification_service.application.service;

import com.project.notification_service.infrastructure.client.UserClient;
import com.project.event_contracts.shared.event.Issues.IssueCreatedEvent;
import com.project.event_contracts.shared.event.Issues.IssueStatusChangedEvent;
import com.project.notification_service.shared.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final MailService mailService;
    private final UserClient userClient;

    public void handleIssueCreated(IssueCreatedEvent event){
        List<String> staffEmails=userClient.getUsersByRole("STAFF")
                .stream()
                .map(UserClient.UserDto::email)
                .toList();

        mailService.notifyStaffAboutNewIssue(
                event.getTitle(),
                event.getDescription(),
                event.getIssueId(),
                staffEmails
        );
    }

    public void handleIssueAssignedOrUpdated(IssueStatusChangedEvent event){
        String citizenEmail=userClient.getUserById(event.getCitizenId()).email();

        mailService.notifyCitizenAboutStatusChange(
                citizenEmail,
                event.getIssueId(),
                Status.valueOf(event.getNewStatus())
        );
    }

}
