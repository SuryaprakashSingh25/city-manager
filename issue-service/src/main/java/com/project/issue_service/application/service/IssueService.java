package com.project.issue_service.application.service;

import com.project.issue_service.application.event.IssueEventPublisher;
import com.project.issue_service.domain.enums.Status;
import com.project.issue_service.domain.model.AttachmentRef;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.domain.model.IssueId;
import com.project.issue_service.domain.repository.IssueRepository;
import com.project.issue_service.exposition.dto.PagedResponse;
import com.project.issue_service.infrastructure.redis.RedisLockService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IssueService {
    private final IssueRepository issueRepository;
    private final RedisLockService redisLockService;
    private final IssueEventPublisher issueEventPublisher;

    public Issue createIssue(String title, String description, String location, String creatorUserId){
        Issue issue=Issue.createDraft(title,description,location,creatorUserId);
        return issueRepository.save(issue);
    }

    public Issue submitIssue(String issueId, String creatorUserId) {
        Issue issue = issueRepository.findById(IssueId.from(issueId))
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if (!issue.getCreatorUserId().equals(creatorUserId)) {
            throw new IllegalStateException("Cannot submit someone else's issue");
        }

        if (!issue.getStatus().equals(Status.DRAFT)) {
            throw new IllegalStateException("Only draft issues can be submitted");
        }

        Issue submitted = issue.submit();
        Issue saved = issueRepository.save(submitted);

        issueEventPublisher.publishIssueCreated(saved);
        return saved;
    }

    public PagedResponse<Issue> getMyIssues(String citizenId, int page, int size, String sortBy, String direction, Status status){
        return issueRepository.findByCreator(citizenId,page,size,sortBy,direction,status);
    }

    public Issue getMyIssueDetails(String citizenId, String issueId){
        return issueRepository.findById(IssueId.from(issueId))
                .filter(issue -> issue.getCreatorUserId().equals(citizenId))
                .orElseThrow(() -> new IllegalArgumentException("Issue not found or not yours"));
    }

    public PagedResponse<Issue> getOpenIssuesForStaff(int page, int size, String sortBy, String direction) {
        return issueRepository.findOpenIssues(page, size, sortBy, direction);
    }

    public Issue getOpenIssueDetails(String issueId, String staffId) {
        Issue issue = issueRepository.findById(IssueId.from(issueId))
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with id: " + issueId));

        if (!issue.getStatus().equals(Status.OPEN)) {
            throw new IllegalArgumentException("Issue is not open");
        }

        boolean locked=redisLockService.acquireLock(issueId,staffId,180);
        if(!locked){
            throw new IllegalStateException("Issue is already being processed by another staff");
        }

        return issue;
    }


    public PagedResponse<Issue> getAssignedIssues(String staffId, int page, int size, String sortBy, String direction, Status status) {
        return issueRepository.findAssignedToStaff(staffId, page, size, sortBy, direction, status);
    }

    public Issue getMyAssignedIssueDetails(String staffId,String issueId){
        return issueRepository.findById(IssueId.from(issueId))
                .filter(issue -> staffId.equals(issue.getAssignedStaffId()))
                .orElseThrow(() -> new IllegalArgumentException("Issue not found or not assigned to you"));
    }

    public Issue acceptIssue(String staffId,String issueId){
        Issue issue=issueRepository.findById(IssueId.from(issueId))
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if (!issue.getStatus().equals(Status.OPEN)) {
            throw new IllegalStateException("Issue is not open and cannot be accepted");
        }

        Issue accepted = issue.accept(staffId);
        Issue saved = issueRepository.save(accepted);

        issueEventPublisher.publishIssueStatusChanged(saved);
        return saved;

    }

    public Issue updateIssueStatus(String staffId, String issueId, Status newStatus, String staffComment){
        Issue issue=issueRepository.findById(IssueId.from(issueId))
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        if(!staffId.equals(issue.getAssignedStaffId())){
            throw new IllegalStateException("Not authorized to update this issue");
        }

        Issue updated=issue.updateStatus(newStatus, staffComment);
        Issue saved =  issueRepository.save(updated);

        issueEventPublisher.publishIssueStatusChanged(saved);
        return saved;

    }

    public Optional<Issue> getById(String id) {
        return issueRepository.findById(IssueId.from(id));
    }

    public void addAttachment(IssueId issueId, AttachmentRef attachment) {
        Issue issue=issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        List<AttachmentRef> updated = new ArrayList<>(issue.getAttachments());
        updated.add(attachment);

        Issue updatedIssue=issue.updateAttachments(updated);
        issueRepository.save(updatedIssue);
    }

    public void removeAttachment(IssueId issueId, String mediaId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        List<AttachmentRef> updated = issue.getAttachments()
                .stream()
                .filter(att -> !att.mediaId().equals(mediaId))
                .toList();

        Issue updatedIssue = issue.updateAttachments(updated);
        issueRepository.save(updatedIssue);
    }

    public boolean isLocked(String issueId) {
        return redisLockService.getLockHolder(issueId) != null;
    }

    public void releaseIssueLock(String staffId, String issueId) {
        redisLockService.releaseLock(issueId, staffId);
    }

}
