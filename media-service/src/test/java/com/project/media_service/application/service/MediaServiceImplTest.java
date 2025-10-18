package com.project.media_service.application.service;

import com.project.media_service.domain.enums.MediaType;
import com.project.media_service.domain.model.Media;
import com.project.media_service.domain.repository.MediaRepository;
import com.project.media_service.infrastructure.feign.IssueClient;
import com.project.media_service.infrastructure.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private IssueClient issueClient;

    @InjectMocks
    private MediaServiceImpl mediaService;

    private MockMultipartFile validFile;

    @BeforeEach
    void setUp() {
        validFile = new MockMultipartFile(
                "file",
                "photo.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
    }

    @Test
    void uploadMedia_ShouldUploadSuccessfully() {
        String issueId = "issue-123";
        String userId = "user-1";

        // Mock issue client
        IssueClient.IssueDto issueDto = new IssueClient.IssueDto(issueId, "Title", "Desc", userId, null, "OPEN");
        when(issueClient.getIssueById(issueId)).thenReturn(issueDto);

        // Mock S3 upload
        when(s3Service.uploadFile(validFile, "issues/" + issueId)).thenReturn("s3/key/photo.png");

        // Mock repository save
        when(mediaRepository.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));

        Media media = mediaService.uploadMedia(issueId, validFile, userId);

        // Assert media fields
        assertNotNull(media);
        assertEquals(issueId, media.getIssueId());
        assertEquals("photo.png", media.getFileName());
        assertEquals(MediaType.IMAGE, media.getFileType());

        // Verify interactions
        ArgumentCaptor<IssueClient.AttachmentRef> captor = ArgumentCaptor.forClass(IssueClient.AttachmentRef.class);
        verify(issueClient).addAttachment(eq(issueId), captor.capture());
        assertEquals(media.getId().getId(), captor.getValue().mediaId());

        verify(mediaRepository).save(any(Media.class));
        verify(s3Service).uploadFile(validFile, "issues/" + issueId);
    }

    @Test
    void uploadMedia_ShouldThrowAccessDenied_WhenNotOwner() {
        String issueId = "issue-123";
        String userId = "user-2";

        IssueClient.IssueDto issueDto = new IssueClient.IssueDto(issueId, "Title", "Desc", "user-1", null, "OPEN");
        when(issueClient.getIssueById(issueId)).thenReturn(issueDto);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> mediaService.uploadMedia(issueId, validFile, userId));
        assertEquals("You can only upload media for your own issues", ex.getMessage());
    }

    @Test
    void uploadMedia_ShouldThrow_WhenFileTooLarge() {
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "big.png", "image/png", new byte[11 * 1024 * 1024]
        );
        String issueId = "issue-1";
        String userId = "user-1";
        when(issueClient.getIssueById(issueId)).thenReturn(new IssueClient.IssueDto(issueId, "Title", "Desc", userId, null, "OPEN"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mediaService.uploadMedia(issueId, largeFile, userId));
        assertEquals("File size exceeds maximum limit of 10 MB", ex.getMessage());
    }

    @Test
    void uploadMedia_ShouldThrow_WhenUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "video.mp4", "video/mp4", new byte[]{1,2}
        );
        String issueId = "issue-1";
        String userId = "user-1";
        when(issueClient.getIssueById(issueId)).thenReturn(new IssueClient.IssueDto(issueId, "Title", "Desc", userId, null, "OPEN"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mediaService.uploadMedia(issueId, file, userId));
        assertEquals("Unsupported media type: video/mp4", ex.getMessage());
    }

    @Test
    void getMediaByIssue_ShouldReturnMediaList() {
        String issueId = "issue-123";
        Media media = Media.createNew(issueId, "photo.png", MediaType.IMAGE, "s3/key/photo.png", "user-1", "image/png");
        when(mediaRepository.findAllByIssueId(issueId)).thenReturn(List.of(media));

        List<Media> mediaList = mediaService.getMediaByIssue(issueId);

        assertEquals(1, mediaList.size());
        assertEquals(media, mediaList.get(0));
    }

    @Test
    void getMediaById_ShouldReturnMedia() {
        Media media = Media.createNew("issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "user-1", "image/png");
        when(mediaRepository.findById(media.getId())).thenReturn(Optional.of(media));

        Media result = mediaService.getMediaById(media.getId().getId());

        assertEquals(media, result);
    }

    @Test
    void getMediaById_ShouldThrow_WhenNotFound() {
        when(mediaRepository.findById(any())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mediaService.getMediaById("id123"));
        assertEquals("Media not found", ex.getMessage());
    }

    @Test
    void deleteMedia_ShouldDeleteSuccessfully() {
        Media media = Media.createNew("issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "user-1", "image/png");
        when(mediaRepository.findById(media.getId())).thenReturn(Optional.of(media));

        mediaService.deleteMedia(media.getId().getId());

        verify(s3Service).deleteFile(media.getS3Key());
        verify(mediaRepository).deleteById(media.getId());
        verify(issueClient).removeAttachment(media.getIssueId(), media.getId().getId());
    }

    @Test
    void downloadMedia_ShouldReturnBytes() {
        Media media = Media.createNew("issue-1", "photo.png", MediaType.IMAGE, "s3/key/photo.png", "user-1", "image/png");
        when(mediaRepository.findById(media.getId())).thenReturn(Optional.of(media));
        when(s3Service.downloadFile(media.getS3Key())).thenReturn(new byte[]{1,2,3});

        byte[] data = mediaService.downloadMedia(media.getId().getId());

        assertArrayEquals(new byte[]{1,2,3}, data);
    }
}
