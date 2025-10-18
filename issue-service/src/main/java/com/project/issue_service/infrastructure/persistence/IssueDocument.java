package com.project.issue_service.infrastructure.persistence;

import com.project.issue_service.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "issues")
public class IssueDocument {
    @Id
    private String id;
    private String title;
    private String description;
    private String location;
    private String creatorUserId;
    private String assignedStaffId;
    private Status status;
    private String staffComment;
    private List<AttachmentRefDocument> attachments;
    private Instant createdAt;
    private Instant updatedAt;
}
