package com.project.issue_service.domain.model;

import com.project.issue_service.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Issue {
    private final IssueId id;
    private final String title;
    private final String description;
    private final String location;
    private final String creatorUserId;
    private final String assignedStaffId;
    private final Status status;
    private final String staffComment;
    private final List<AttachmentRef> attachments;
    private final Instant createdAt;
    private final Instant updatedAt;

    public static Issue createDraft(String title, String description,String location, String creatorUserId){
        return Issue.builder()
                .id(IssueId.newId())
                .title(title)
                .description(description)
                .location(location)
                .creatorUserId(creatorUserId)
                .status(Status.DRAFT)
                .attachments(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public Issue submit() {
        if (!this.status.equals(Status.DRAFT)) {
            throw new IllegalStateException("Only draft issues can be submitted");
        }
        return new Issue(
                this.id,
                this.title,
                this.description,
                this.location,
                this.creatorUserId,
                this.assignedStaffId,
                Status.OPEN,
                this.staffComment,
                this.attachments,
                this.createdAt,
                Instant.now()
        );
    }

    public Issue accept(String staffId){
        if(this.status!=Status.OPEN){
            throw new IllegalStateException("Issue already accepted or closed");
        }
        return Issue.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .location(this.location)
                .creatorUserId(this.creatorUserId)
                .assignedStaffId(staffId)
                .status(Status.IN_PROGRESS)
                .attachments(this.attachments)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .build();
    }

    public Issue updateStatus(Status newStatus, String staffComment){
        if(this.status==Status.RESOLVED || this.status==Status.REJECTED){
            throw new IllegalStateException("Cannot update a closed issue");
        }
        return Issue.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .location(this.location)
                .creatorUserId(this.creatorUserId)
                .assignedStaffId(this.assignedStaffId)
                .status(newStatus)
                .staffComment(staffComment)
                .attachments(this.attachments)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .build();
    }

    public Issue updateAttachments(List<AttachmentRef> updated) {
        return Issue.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .location(this.location)
                .creatorUserId(this.creatorUserId)
                .assignedStaffId(this.assignedStaffId)
                .status(this.status)
                .staffComment(this.staffComment)
                .attachments(updated)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .build();
    }
}
