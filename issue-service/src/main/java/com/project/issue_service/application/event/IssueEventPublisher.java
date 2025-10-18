package com.project.issue_service.application.event;

import com.project.issue_service.domain.model.Issue;

public interface IssueEventPublisher {
    void publishIssueCreated(Issue issue);
    void publishIssueStatusChanged(Issue issue);
}
