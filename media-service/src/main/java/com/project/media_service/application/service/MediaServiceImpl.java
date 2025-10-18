package com.project.media_service.application.service;

import com.project.media_service.application.port.in.MediaService;
import com.project.media_service.domain.enums.MediaType;
import com.project.media_service.domain.model.Media;
import com.project.media_service.domain.model.MediaId;
import com.project.media_service.domain.repository.MediaRepository;
import com.project.media_service.infrastructure.feign.IssueClient;
import com.project.media_service.infrastructure.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final S3Service s3Service;
    private final IssueClient issueClient;

    @Override
    public Media uploadMedia(String issueId, MultipartFile file, String uploadedBy) {

        IssueClient.IssueDto issue = issueClient.getIssueById(issueId);

        if (!issue.creatorUserId().equals(uploadedBy)) {
            throw new AccessDeniedException("You can only upload media for your own issues");
        }

        long maxSize = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("File must have a valid content type");
        }

        MediaType mediaType;
        if (contentType.startsWith("image")) {
            mediaType = MediaType.IMAGE;
        } else {
            throw new IllegalArgumentException("Unsupported media type: " + contentType);
        }

        String s3Key = s3Service.uploadFile(file, "issues/" + issueId);

        Media media = Media.createNew(
                issueId,
                file.getOriginalFilename(),
                mediaType,
                s3Key,
                uploadedBy,
                file.getContentType()
        );
        Media saved=mediaRepository.save(media);

        IssueClient.AttachmentRef ref=new IssueClient.AttachmentRef(
                saved.getId().getId()
        );
        issueClient.addAttachment(issueId,ref);
        return saved;
    }

    @Override
    public List<Media> getMediaByIssue(String issueId) {
        return mediaRepository.findAllByIssueId(issueId);
    }

    @Override
    public Media getMediaById(String mediaId) {
        return mediaRepository.findById(MediaId.from(mediaId))
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
    }

    @Override
    public void deleteMedia(String mediaId) {
        Media media = mediaRepository.findById(MediaId.from(mediaId))
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));

        s3Service.deleteFile(media.getS3Key());
        mediaRepository.deleteById(media.getId());
        issueClient.removeAttachment(media.getIssueId(), mediaId);
    }

    @Override
    public byte[] downloadMedia(String mediaId) {
        Media media = getMediaById(mediaId);
        return s3Service.downloadFile(media.getS3Key());
    }

}
