package com.project.media_service.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringMediaRepository extends MongoRepository<MediaDocument, String> {
    List<MediaDocument> findAllByIssueId(String issueId);
}
