package com.project.issue_service.exposition.dto;

import com.project.issue_service.domain.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class IssueResponse {
    private String id;
    private String title;
    private String description;
    private String location;
    private String creatorUserId;
    private String assignedStaffId;
    private Status status;
    private String staffComment;
    private List<String> attachmentUrls;
    private Instant createdAt;
    private Instant updatedAt;
}
