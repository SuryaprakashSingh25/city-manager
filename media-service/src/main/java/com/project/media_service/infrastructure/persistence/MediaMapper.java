package com.project.media_service.infrastructure.persistence;

import com.project.media_service.domain.model.Media;
import com.project.media_service.domain.model.MediaId;

public class MediaMapper {

    public static MediaDocument toDocument(Media media) {
        return MediaDocument.builder()
                .id(media.getId().getId())
                .issueId(media.getIssueId())
                .fileName(media.getFileName())
                .fileType(media.getFileType())
                .s3Key(media.getS3Key())
                .uploadedByUserId(media.getUploadedByUserId())
                .uploadedAt(media.getUploadedAt())
                .build();
    }

    public static Media toDomain(MediaDocument document) {
        return  Media.builder()
                .id(MediaId.from(document.getId()))
                .issueId(document.getIssueId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .s3Key(document.getS3Key())
                .uploadedByUserId(document.getUploadedByUserId())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

}
