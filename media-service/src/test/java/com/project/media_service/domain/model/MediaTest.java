package com.project.media_service.domain.model;

import com.project.media_service.domain.enums.MediaType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {

    @Test
    void createNew_ShouldCreateMediaSuccessfully() {
        Media media = Media.createNew(
                "issue-123",
                "photo.png",
                MediaType.IMAGE,
                "s3/key/photo.png",
                "user-1",
                "image/png"
        );

        assertNotNull(media.getId());
        assertEquals("issue-123", media.getIssueId());
        assertEquals("photo.png", media.getFileName());
        assertEquals(MediaType.IMAGE, media.getFileType());
        assertEquals("s3/key/photo.png", media.getS3Key());
        assertEquals("user-1", media.getUploadedByUserId());
        assertEquals("image/png", media.getContentType());
        assertNotNull(media.getUploadedAt());
        assertTrue(media.getUploadedAt() instanceof Instant);
    }

    @Test
    void createNew_ShouldThrow_WhenIssueIdIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Media.createNew("", "file.png", MediaType.IMAGE, "s3/key/file.png", "user-1", "image/png")
        );
        assertEquals("IssueId must not be null or blank", ex.getMessage());
    }

    @Test
    void createNew_ShouldThrow_WhenFileNameIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Media.createNew("issue-1", "", MediaType.IMAGE, "s3/key/file.png", "user-1", "image/png")
        );
        assertEquals("FileName must not be null or blank", ex.getMessage());
    }

    @Test
    void createNew_ShouldThrow_WhenFileTypeIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Media.createNew("issue-1", "file.png", null, "s3/key/file.png", "user-1", "image/png")
        );
        assertEquals("FileType must not be null", ex.getMessage());
    }

    @Test
    void createNew_ShouldThrow_WhenS3KeyIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Media.createNew("issue-1", "file.png", MediaType.IMAGE, "", "user-1", "image/png")
        );
        assertEquals("S3 key must not be null or blank", ex.getMessage());
    }

    @Test
    void createNew_ShouldThrow_WhenUploaderIdIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Media.createNew("issue-1", "file.png", MediaType.IMAGE, "s3/key/file.png", "", "image/png")
        );
        assertEquals("Uploader userId must not be null or blank", ex.getMessage());
    }

    @Test
    void createNew_ShouldThrow_WhenContentTypeIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Media.createNew("issue-1", "file.png", MediaType.IMAGE, "s3/key/file.png", "user-1", "")
        );
        assertEquals("ContentType must not be null or blank", ex.getMessage());
    }

    @Test
    void createNew_ShouldThrow_WhenFileExtensionDoesNotMatchMediaType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Media.createNew("issue-1", "file.pdf", MediaType.IMAGE, "s3/key/file.pdf", "user-1", "application/pdf")
        );
        assertTrue(ex.getMessage().contains("File extension does not match MediaType"));
    }
}
