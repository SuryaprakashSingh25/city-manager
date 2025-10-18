package com.project.issue_service.infrastructure.persistence;

import com.project.issue_service.domain.model.AttachmentRef;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.domain.model.IssueId;

import java.util.stream.Collectors;

public class IssueMapper {

    public static IssueDocument toDocument(Issue issue){
        return IssueDocument.builder()
                .id(issue.getId().getValue())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .location((issue.getLocation()))
                .creatorUserId(issue.getCreatorUserId())
                .assignedStaffId(issue.getAssignedStaffId())
                .status(issue.getStatus())
                .staffComment(issue.getStaffComment())
                .attachments(issue.getAttachments().stream()
                        .map(a -> new AttachmentRefDocument(a.mediaId()))
                        .collect(Collectors.toList()))
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }

    public static Issue toDomain(IssueDocument doc){
        return Issue.builder()
                .id(IssueId.from(doc.getId()))
                .title(doc.getTitle())
                .description(doc.getDescription())
                .location((doc.getLocation()))
                .creatorUserId(doc.getCreatorUserId())
                .assignedStaffId(doc.getAssignedStaffId())
                .status(doc.getStatus())
                .staffComment(doc.getStaffComment())
                .attachments(doc.getAttachments().stream()
                        .map(a -> new AttachmentRef(a.getMediaId()))
                        .collect(Collectors.toList()))
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

}
