package com.project.media_service.infrastructure.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "issue-service")
public interface IssueClient {

    @GetMapping("/api/internal/issues/{issueId}")
    IssueDto getIssueById(@PathVariable("issueId") String issueId);

    @PostMapping("/api/internal/issues/{issueId}/attachments")
    void addAttachment(
            @PathVariable("issueId") String issueId,
            @RequestBody AttachmentRef attachment);

    @DeleteMapping("/api/internal/issues/{issueId}/attachments/{mediaId}")
    void removeAttachment(
            @PathVariable("issueId") String issueId,
            @PathVariable("mediaId") String mediaId
    );


    record AttachmentRef(String mediaId) {}

    record IssueDto(
            String issueId,
            String title,
            String description,
            String creatorUserId,
            String assignedStaffId,
            String status
    ) {}
}

