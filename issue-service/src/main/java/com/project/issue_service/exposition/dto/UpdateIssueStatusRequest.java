package com.project.issue_service.exposition.dto;

import com.project.issue_service.domain.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateIssueStatusRequest {
    @NotNull(message = "Status must not be null")
    private Status status;
    private String staffComment;
}
