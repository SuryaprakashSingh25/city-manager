package com.project.media_service.application.port.in;

import com.project.media_service.domain.model.Media;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {

    Media uploadMedia(String issueId, MultipartFile file, String uploadedBy);

    List<Media> getMediaByIssue(String issueId);

    Media getMediaById(String mediaId);

    void deleteMedia(String mediaId);

    byte[] downloadMedia(String mediaId);

}
