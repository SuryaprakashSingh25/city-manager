package com.project.issue_service.exposition.controller;

import com.project.issue_service.application.service.IssueService;
import com.project.issue_service.domain.model.AttachmentRef;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.domain.model.IssueId;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/issues")
@RequiredArgsConstructor
public class InternalIssueController {

    private final IssueService issueService;

    @GetMapping("/{issueId}")
    public Issue getIssueById(@PathVariable("issueId") String issueId) {
        return issueService.getById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    }

    @PostMapping("/{issueId}/attachments")
    public ResponseEntity<Void> addAttachment(
            @PathVariable("issueId") String issueId,
            @RequestBody AttachmentRef attachment) {
        issueService.addAttachment(IssueId.from(issueId), attachment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{issueId}/attachments/{mediaId}")
    public ResponseEntity<Void> removeAttachment(
            @PathVariable("issueId") String issueId,
            @PathVariable("mediaId") String mediaId) {
        issueService.removeAttachment(IssueId.from(issueId), mediaId);
        return ResponseEntity.noContent().build();
    }



    record IssueDto(
            String issueId,
            String title,
            String description,
            String creatorUserId,
            String assignedStaffId,
            String status
    ) {}
}

