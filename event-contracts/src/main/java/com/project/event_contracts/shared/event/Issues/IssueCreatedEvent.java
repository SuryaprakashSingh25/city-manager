package com.project.event_contracts.shared.event.Issues;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueCreatedEvent {
    private String issueId;
    private String title;
    private String description;
    private String citizenId;
}
