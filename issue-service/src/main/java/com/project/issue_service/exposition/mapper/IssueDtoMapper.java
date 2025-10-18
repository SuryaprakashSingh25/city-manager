package com.project.issue_service.exposition.mapper;

import com.project.issue_service.domain.model.AttachmentRef;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.exposition.dto.IssueResponse;

import java.util.stream.Collectors;

public class IssueDtoMapper {
    private IssueDtoMapper(){}

    public static IssueResponse toDto(Issue issue){
        return IssueResponse.builder()
                .id(issue.getId().getValue())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .location(issue.getLocation())
                .creatorUserId(issue.getCreatorUserId())
                .assignedStaffId(issue.getAssignedStaffId())
                .status(issue.getStatus())
                .staffComment(issue.getStaffComment())
                .attachmentUrls(issue.getAttachments()
                        .stream()
                        .map(AttachmentRef::mediaId)
                        .collect(Collectors.toList()))
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .build();
    }

}
