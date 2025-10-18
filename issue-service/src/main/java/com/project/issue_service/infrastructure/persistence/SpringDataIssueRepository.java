package com.project.issue_service.infrastructure.persistence;

import com.project.issue_service.domain.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataIssueRepository extends MongoRepository<IssueDocument, String> {
    Page<IssueDocument> findByCreatorUserId(String creatorUserId, Pageable pageable);

    Page<IssueDocument> findByCreatorUserIdAndStatus(String creatorUserId, Status status, Pageable pageable);

    Page<IssueDocument> findByAssignedStaffId(String staffId, Pageable pageable);
    Page<IssueDocument> findByAssignedStaffIdAndStatus(String staffId, Status status, Pageable pageable);

    Page<IssueDocument> findByStatus(Status status, Pageable pageable);

}
