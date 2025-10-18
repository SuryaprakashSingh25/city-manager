package com.project.issue_service.infrastructure.kafka;

import com.project.event_contracts.shared.event.Issues.IssueCreatedEvent;
import com.project.event_contracts.shared.event.Issues.IssueStatusChangedEvent;
import com.project.issue_service.application.event.IssueEventPublisher;
import com.project.issue_service.domain.model.Issue;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaIssueEventPublisher implements IssueEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ISSUE_CREATED_TOPIC = "issue-create";
    private static final String ISSUE_STATUS_CHANGED_TOPIC = "issue-status-change";

    @Override
    public void publishIssueCreated(Issue issue) {
        IssueCreatedEvent event = new IssueCreatedEvent(
                issue.getId().getValue(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getCreatorUserId()
        );
        kafkaTemplate.send(ISSUE_CREATED_TOPIC, issue.getId().getValue(), event);
    }

    @Override
    public void publishIssueStatusChanged(Issue issue) {
        IssueStatusChangedEvent event=new IssueStatusChangedEvent(
                issue.getId().getValue(),
                String.valueOf(issue.getStatus()),
                issue.getStaffComment(),
                issue.getCreatorUserId()
        );
        kafkaTemplate.send(ISSUE_STATUS_CHANGED_TOPIC, issue.getId().getValue(), event);
    }
}
