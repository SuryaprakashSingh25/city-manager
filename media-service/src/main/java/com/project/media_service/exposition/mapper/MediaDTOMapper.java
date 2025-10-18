package com.project.media_service.exposition.mapper;

import com.project.media_service.domain.model.Media;
import com.project.media_service.exposition.dto.MediaResponse;
import com.project.media_service.infrastructure.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MediaDTOMapper {

    private final static int PRESIGNED_URL_EXPIRATION_MINUTES = 10;
    private final S3Service s3Service;

    public MediaResponse from(Media media){

        String presignedUrl = s3Service.generatePresignedUrl(media.getS3Key(), PRESIGNED_URL_EXPIRATION_MINUTES);

        return MediaResponse.builder()
                .id(media.getId().getId())
                .issueId(media.getIssueId())
                .fileName(media.getFileName())
                .mediaType(media.getFileType().name())
                .contentType(media.getContentType())
                .uploadedBy(media.getUploadedByUserId())
                .url(presignedUrl)
                .build();
    }
}
