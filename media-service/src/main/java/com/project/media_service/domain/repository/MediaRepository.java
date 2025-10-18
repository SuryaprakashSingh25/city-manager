package com.project.media_service.domain.repository;

import com.project.media_service.domain.model.Media;
import com.project.media_service.domain.model.MediaId;

import java.util.List;
import java.util.Optional;

public interface MediaRepository {
    Media save(Media media);

    List<Media> saveAll(List<Media> mediaList);

    Optional<Media> findById(MediaId id);

    List<Media> findAllByIssueId(String issueId);

    boolean existsById(MediaId id);

    void deleteById(MediaId id);

    void deleteByIssueId(String issueId);
}

