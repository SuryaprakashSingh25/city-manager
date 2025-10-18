package com.project.media_service.infrastructure.persistence;

import com.project.media_service.domain.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDocument {
    @Id
    private String id;
    private String issueId;
    private String fileName;
    private MediaType fileType;
    private String s3Key;
    private String uploadedByUserId;
    private Instant uploadedAt;
}
