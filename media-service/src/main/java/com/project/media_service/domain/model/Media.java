package com.project.media_service.domain.model;

import com.project.media_service.domain.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class Media {
    private final MediaId id;
    private final String issueId;
    private final String fileName;
    private final MediaType fileType;
    private final String s3Key;
    private final String uploadedByUserId;
    private final Instant uploadedAt;
    private final String contentType;

    private Media(MediaId id, String issueId, String fileName, MediaType fileType,
                  String s3Key, String uploadedByUserId, Instant uploadedAt, String contentType){
        this.id=id;
        this.issueId=issueId;
        this.fileName=fileName;
        this.fileType=fileType;
        this.s3Key=s3Key;
        this.uploadedByUserId=uploadedByUserId;
        this.uploadedAt=uploadedAt;
        this.contentType = contentType;
    }

    public static Media createNew(String issueId, String fileName, MediaType fileType,
                                  String s3Key, String uploadedByUserId, String contentType){
        if (issueId == null || issueId.isBlank()) {
            throw new IllegalArgumentException("IssueId must not be null or blank");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("FileName must not be null or blank");
        }
        if (fileType == null) {
            throw new IllegalArgumentException("FileType must not be null");
        }
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("S3 key must not be null or blank");
        }
        if (uploadedByUserId == null || uploadedByUserId.isBlank()) {
            throw new IllegalArgumentException("Uploader userId must not be null or blank");
        }
        if (!fileType.isValidExtension(fileName)) {
            throw new IllegalArgumentException(
                    "File extension does not match MediaType " + fileType + ": " + fileName
            );
        }

        if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("ContentType must not be null or blank");

        return new Media(MediaId.generate(),issueId,fileName,fileType,s3Key,uploadedByUserId,Instant.now(), contentType);
    }

}
