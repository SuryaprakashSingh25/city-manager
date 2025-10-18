package com.project.media_service.exposition.controller;

import com.project.media_service.application.port.in.MediaService;
import com.project.media_service.domain.enums.MediaType;
import com.project.media_service.domain.model.Media;
import com.project.media_service.exposition.dto.MediaResponse;
import com.project.media_service.exposition.mapper.MediaDTOMapper;
import com.project.media_service.infrastructure.feign.IssueClient;
import com.project.media_service.infrastructure.security.CustomUserDetails;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final MediaDTOMapper mediaDTOMapper;
    private final IssueClient issueClient;

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping("/upload/{issueId}")
    public ResponseEntity<MediaResponse> uploadMedia(
            @PathVariable("issueId") String issueId,
            @RequestParam("file") MultipartFile file) {
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Media media = mediaService.uploadMedia(issueId, file, user.getUserId());
        return ResponseEntity.ok(mediaDTOMapper.from(media));
    }


    @PreAuthorize("hasAnyRole('STAFF','CITIZEN')")
    @GetMapping("/issues/{issueId}")
    public ResponseEntity<List<MediaResponse>> getMediaByIssue(@PathVariable("issueId") String issueId){
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        List<Media> mediaList = mediaService.getMediaByIssue(issueId);
        if (user.getRole().equals("CITIZEN")) {
            mediaList = mediaList.stream()
                    .filter(media -> media.getUploadedByUserId().equals(user.getUserId()))
                    .toList();
        }

        List<MediaResponse> response = mediaList.stream()
                .map(mediaDTOMapper::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(("hasAnyRole('STAFF','CITIZEN')"))
    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaResponse> getMediaById(@PathVariable("mediaId") String mediaId){
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Media media=mediaService.getMediaById(mediaId);

        if(user.getRole().equals("CITIZEN") &&
            !media.getUploadedByUserId().equals(user.getUserId())){
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(mediaDTOMapper.from(media));

    }

    @PreAuthorize("hasAnyRole('STAFF', 'CITIZEN')")
    @GetMapping("/download/{mediaId}")
    public ResponseEntity<Resource> downloadMedia(@PathVariable("mediaId") String mediaId){
        CustomUserDetails user = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        Media media=mediaService.getMediaById(mediaId);

        if(user.getRole().equals("CITIZEN") &&
            !media.getUploadedByUserId().equals(user.getUserId())){
            return ResponseEntity.status(403).build();
        }

        byte[] filesBytes= mediaService.downloadMedia(mediaId);

        ByteArrayResource resource=new ByteArrayResource(filesBytes);

        org.springframework.http.MediaType contentType = org.springframework.http.MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + media.getFileName() + "\"")
                .body(resource);

    }

    @PreAuthorize("hasRole('CITIZEN')")
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable("mediaId") String mediaId) {
        mediaService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

}
