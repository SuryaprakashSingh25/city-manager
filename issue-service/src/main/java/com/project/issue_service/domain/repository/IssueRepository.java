package com.project.issue_service.domain.repository;

import com.project.issue_service.domain.enums.Status;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.domain.model.IssueId;
import com.project.issue_service.exposition.dto.PagedResponse;

import java.util.List;
import java.util.Optional;

public interface IssueRepository {
    Issue save(Issue issue);
    Optional<Issue> findById(IssueId id);
    PagedResponse<Issue> findByCreator(String creatorUserId, int page, int size, String sortBy, String direction, Status status);
    PagedResponse<Issue> findOpenIssues(int page, int size, String sortBy, String direction);
    PagedResponse<Issue> findAssignedToStaff(String staffId, int page, int size, String sortBy, String direction, Status status);
}