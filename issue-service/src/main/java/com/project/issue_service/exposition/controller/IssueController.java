package com.project.issue_service.exposition.controller;

import com.project.issue_service.application.service.IssueService;
import com.project.issue_service.domain.enums.Status;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.exposition.dto.CreateIssueRequest;
import com.project.issue_service.exposition.dto.IssueResponse;
import com.project.issue_service.exposition.dto.PagedResponse;
import com.project.issue_service.exposition.dto.UpdateIssueStatusRequest;
import com.project.issue_service.exposition.mapper.IssueDtoMapper;
import com.project.issue_service.infrastructure.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    // ------------------- Citizen Endpoints -------------------

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@RequestBody CreateIssueRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails user) {
        Issue issue = issueService.createIssue(request.getTitle(), request.getDescription(), request.getLocation(), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(IssueDtoMapper.toDto(issue));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PutMapping("/{id}/submit")
    public ResponseEntity<IssueResponse> submitIssue(@PathVariable("id") String id,
                                                     @AuthenticationPrincipal CustomUserDetails user) {
        Issue issue = issueService.submitIssue(id, user.getUserId());
        return ResponseEntity.ok(IssueDtoMapper.toDto(issue));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<IssueResponse>> getMyIssues(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction,
            @RequestParam(name = "status", required = false) Status status
            ) {
        PagedResponse<Issue> pagedIssues = issueService.getMyIssues(user.getUserId(),page,size,sortBy,direction,status);

        List<IssueResponse> dtos=pagedIssues.getContent().stream()
                .filter(issue -> !issue.getStatus().equals(Status.DRAFT))
                .map(IssueDtoMapper::toDto)
                .toList();

        PagedResponse<IssueResponse> response = new PagedResponse<>(
                dtos,
                pagedIssues.getCurrentPage(),
                pagedIssues.getTotalPages(),
                pagedIssues.getTotalItems()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my/{id}")
    public ResponseEntity<IssueResponse> getMyIssueDetails(@PathVariable("id") String id,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        Issue issue=issueService.getMyIssueDetails(user.getUserId(), id);
        return ResponseEntity.ok(IssueDtoMapper.toDto(issue));
    }

    // ------------------- Staff Endpoints -------------------

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/open")
    public ResponseEntity<PagedResponse<IssueResponse>> getOpenIssues(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction
    ) {
        PagedResponse<Issue> pagedIssues = issueService.getOpenIssuesForStaff(page, size, sortBy, direction);

        List<IssueResponse> dtos = pagedIssues.getContent().stream()
                .map(IssueDtoMapper::toDto)
                .toList();

        PagedResponse<IssueResponse> response = new PagedResponse<>(
                dtos,
                pagedIssues.getCurrentPage(),
                pagedIssues.getTotalPages(),
                pagedIssues.getTotalItems()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/open/{id}")
    public ResponseEntity<IssueResponse> getOpenIssueDetails(@PathVariable("id") String id, @AuthenticationPrincipal CustomUserDetails user) {
        try {
            Issue issue = issueService.getOpenIssueDetails(id, user.getUserId());
            return ResponseEntity.ok(IssueDtoMapper.toDto(issue));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        }
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/assigned")
    public ResponseEntity<PagedResponse<IssueResponse>> getAssignedIssues(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sortBy", defaultValue = "updatedAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction,
            @RequestParam(name = "status", required = false) Status status
    ) {
        PagedResponse<Issue> pagedIssues = issueService.getAssignedIssues(user.getUserId(), page, size, sortBy, direction, status);

        List<IssueResponse> dtos = pagedIssues.getContent().stream()
                .map(IssueDtoMapper::toDto)
                .toList();

        PagedResponse<IssueResponse> response = new PagedResponse<>(
                dtos,
                pagedIssues.getCurrentPage(),
                pagedIssues.getTotalPages(),
                pagedIssues.getTotalItems()
        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/assigned/{id}")
    public ResponseEntity<IssueResponse> getAssignedIssueDetails(@PathVariable("id") String id,
                                                 @AuthenticationPrincipal CustomUserDetails user) {
        Issue issue = issueService.getMyAssignedIssueDetails(user.getUserId(), id);
        return ResponseEntity.ok(IssueDtoMapper.toDto(issue));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/accept")
    public ResponseEntity<IssueResponse> acceptIssue(@PathVariable("id") String id,
                                     @AuthenticationPrincipal CustomUserDetails user) {
        Issue issue = issueService.acceptIssue(user.getUserId(), id);
        return ResponseEntity.ok(IssueDtoMapper.toDto(issue));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateIssueStatus(@PathVariable("id") String id,
                                           @RequestBody UpdateIssueStatusRequest request,
                                           @AuthenticationPrincipal CustomUserDetails user) {
        Issue issue=issueService.updateIssueStatus(user.getUserId(),id, request.getStatus(), request.getStaffComment());
        return ResponseEntity.ok(IssueDtoMapper.toDto(issue));
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/open/locks")
    public ResponseEntity<Map<String, Boolean>> getOpenIssueLocks(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,  // default limit
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "DESC") String direction
    ) {
        // Get paginated open issues
        PagedResponse<Issue> pagedIssues = issueService.getOpenIssuesForStaff(page, size, sortBy, direction);

        Map<String, Boolean> locks = new HashMap<>();
        for (Issue issue : pagedIssues.getContent()) {
            locks.put(issue.getId().getValue(), issueService.isLocked(issue.getId().getValue()));
        }

        return ResponseEntity.ok(locks);
    }


    @PreAuthorize("hasRole('STAFF')")
    @DeleteMapping("/open/{id}/lock")
    public ResponseEntity<Void> releaseIssueLock(@PathVariable("id") String id,
                                                 @AuthenticationPrincipal CustomUserDetails user) {
        issueService.releaseIssueLock(user.getUserId(), id);
        return ResponseEntity.noContent().build();
    }

}
