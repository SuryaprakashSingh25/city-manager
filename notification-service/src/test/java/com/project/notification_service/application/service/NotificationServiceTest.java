package com.project.notification_service.application.service;

import com.project.notification_service.infrastructure.client.UserClient;
import com.project.event_contracts.shared.event.Issues.IssueCreatedEvent;
import com.project.event_contracts.shared.event.Issues.IssueStatusChangedEvent;
import com.project.notification_service.shared.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private MailService mailService;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private NotificationService notificationService;

    private IssueCreatedEvent issueCreatedEvent;
    private IssueStatusChangedEvent issueStatusChangedEvent;

    @BeforeEach
    void setUp() {
        issueCreatedEvent = new IssueCreatedEvent();
        issueCreatedEvent.setTitle("Pothole on road");
        issueCreatedEvent.setDescription("Large pothole near main street");
        issueCreatedEvent.setIssueId("issue-123");

        issueStatusChangedEvent = new IssueStatusChangedEvent();
        issueStatusChangedEvent.setIssueId("issue-123");
        issueStatusChangedEvent.setCitizenId("citizen-1");
        issueStatusChangedEvent.setNewStatus(Status.IN_PROGRESS.name());
    }

    @Test
    void testHandleIssueCreated_ShouldNotifyStaff() {
        // given
        UserClient.UserDto staff1 = new UserClient.UserDto("1", "staff1@example.com", "STAFF","OPEN");
        UserClient.UserDto staff2 = new UserClient.UserDto("2", "staff2@example.com", "STAFF","OPEN");

        when(userClient.getUsersByRole("STAFF")).thenReturn(List.of(staff1, staff2));

        // when
        notificationService.handleIssueCreated(issueCreatedEvent);

        // then
        verify(mailService, times(1)).notifyStaffAboutNewIssue(
                eq("Pothole on road"),
                eq("Large pothole near main street"),
                eq("issue-123"),
                eq(List.of("staff1@example.com", "staff2@example.com"))
        );
    }

    @Test
    void testHandleIssueAssignedOrUpdated_ShouldNotifyCitizen() {
        // given
        UserClient.UserDto citizen = new UserClient.UserDto("citizen-1", "citizen@example.com", "CITIZEN","OPEN");
        when(userClient.getUserById("citizen-1")).thenReturn(citizen);

        // when
        notificationService.handleIssueAssignedOrUpdated(issueStatusChangedEvent);

        // then
        ArgumentCaptor<Status> statusCaptor = ArgumentCaptor.forClass(Status.class);

        verify(mailService, times(1)).notifyCitizenAboutStatusChange(
                eq("citizen@example.com"),
                eq("issue-123"),
                statusCaptor.capture()
        );

        assertEquals(Status.IN_PROGRESS, statusCaptor.getValue());
    }
}
