package com.project.media_service.exposition.controller;

import com.project.media_service.application.port.in.MediaService;
import com.project.media_service.domain.enums.MediaType;
import com.project.media_service.domain.model.Media;
import com.project.media_service.exposition.dto.MediaResponse;
import com.project.media_service.exposition.mapper.MediaDTOMapper;
import com.project.media_service.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @Mock
    private MediaDTOMapper mediaDTOMapper;

    @InjectMocks
    private MediaController mediaController;

    private MockMvc mockMvc;

    private CustomUserDetails citizenUser;
    private CustomUserDetails staffUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mediaController).build();
        citizenUser = new CustomUserDetails("user-1", "CITIZEN");
        staffUser = new CustomUserDetails("staff-1", "STAFF");
    }

    private void mockAuthentication(CustomUserDetails user) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void uploadMedia_ShouldReturnMediaResponse() throws Exception {
        mockAuthentication(citizenUser);

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", new byte[]{1, 2, 3});

        Media media = Media.createNew(
                "issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "user-1", "image/png");

        MediaResponse response = MediaResponse.builder()
                .id(media.getId().getId())
                .issueId(media.getIssueId())
                .fileName(media.getFileName())
                .mediaType(media.getFileType().name())
                .contentType(media.getContentType())
                .uploadedBy(media.getUploadedByUserId())
                .url("https://cdn.example.com/" + media.getFileName())
                .build();

        when(mediaService.uploadMedia("issue-1", file, "user-1")).thenReturn(media);
        when(mediaDTOMapper.from(media)).thenReturn(response);

        mockMvc.perform(multipart("/api/media/upload/issue-1").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(media.getId().getId()))
                .andExpect(jsonPath("$.fileName").value("photo.png"))
                .andExpect(jsonPath("$.mediaType").value("IMAGE"));
    }

    @Test
    void getMediaByIssue_CitizenFiltersOwn() throws Exception {
        mockAuthentication(citizenUser);

        Media m1 = Media.createNew("issue-1", "a.png", MediaType.IMAGE, "s3/key/a.png", "user-1", "image/png");
        Media m2 = Media.createNew("issue-1", "b.png", MediaType.IMAGE, "s3/key/b.png", "user-2", "image/png");

        when(mediaService.getMediaByIssue("issue-1")).thenReturn(List.of(m1, m2));
        when(mediaDTOMapper.from(m1)).thenReturn(
                MediaResponse.builder()
                        .id(m1.getId().getId())
                        .issueId(m1.getIssueId())
                        .fileName(m1.getFileName())
                        .mediaType(m1.getFileType().name())
                        .contentType(m1.getContentType())
                        .uploadedBy(m1.getUploadedByUserId())
                        .url("https://cdn.example.com/" + m1.getFileName())
                        .build()
        );

        mockMvc.perform(get("/api/media/issues/issue-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fileName").value("a.png"));
    }

    @Test
    void getMediaById_CitizenDenied() throws Exception {
        mockAuthentication(citizenUser);

        Media media = Media.createNew("issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "other-user", "image/png");
        when(mediaService.getMediaById("media-1")).thenReturn(media);

        mockMvc.perform(get("/api/media/media-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMediaById_CitizenAllowed() throws Exception {
        mockAuthentication(citizenUser);

        Media media = Media.createNew("issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "user-1", "image/png");
        MediaResponse response = MediaResponse.builder()
                .id(media.getId().getId())
                .issueId(media.getIssueId())
                .fileName(media.getFileName())
                .mediaType(media.getFileType().name())
                .contentType(media.getContentType())
                .uploadedBy(media.getUploadedByUserId())
                .url("https://cdn.example.com/" + media.getFileName())
                .build();

        when(mediaService.getMediaById("media-1")).thenReturn(media);
        when(mediaDTOMapper.from(media)).thenReturn(response);

        mockMvc.perform(get("/api/media/media-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("photo.png"));
    }

    @Test
    void downloadMedia_CitizenDenied() throws Exception {
        mockAuthentication(citizenUser);

        Media media = Media.createNew("issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "other-user", "image/png");
        when(mediaService.getMediaById("media-1")).thenReturn(media);

        mockMvc.perform(get("/api/media/download/media-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadMedia_CitizenAllowed() throws Exception {
        mockAuthentication(citizenUser);

        Media media = Media.createNew("issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "user-1", "image/png");
        when(mediaService.getMediaById("media-1")).thenReturn(media);
        when(mediaService.downloadMedia("media-1")).thenReturn(new byte[]{1,2,3});

        mockMvc.perform(get("/api/media/download/media-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + media.getFileName() + "\""));
    }

    @Test
    void deleteMedia_ShouldCallService() throws Exception {

        mockMvc.perform(delete("/api/media/media-1"))
                .andExpect(status().isNoContent());

        verify(mediaService).deleteMedia("media-1");
    }
}
