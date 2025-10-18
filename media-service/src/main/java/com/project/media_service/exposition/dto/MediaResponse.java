package com.project.media_service.exposition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MediaResponse {
    String id;
    String issueId;
    String fileName;
    String mediaType;
    String contentType;
    String uploadedBy;
    String url;
}
