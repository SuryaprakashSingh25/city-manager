package com.project.media_service.domain.enums;

import java.util.Arrays;

public enum MediaType {
    IMAGE(
            new String[]{"image/png", "image/jpeg", "image/jpg"},
            new String[]{".png", ".jpg", ".jpeg"}
    );

    private final String[] mimeTypes;
    private final String[] extensions;

    MediaType(String[] mimeTypes, String[] extensions) {
        this.mimeTypes = mimeTypes;
        this.extensions = extensions;
    }

    public String[] getMimeTypes() {
        return mimeTypes;
    }

    public String[] getExtensions() {
        return extensions;
    }

    /** Detect MediaType from MIME type */
    public static MediaType fromMimeType(String mimeType) {
        for (MediaType type : values()) {
            if (Arrays.stream(type.mimeTypes).anyMatch(mimeType::equalsIgnoreCase)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
    }

    /** Detect MediaType from file extension */
    public static MediaType fromFileName(String fileName) {
        String lower = fileName.toLowerCase();
        for (MediaType type : values()) {
            if (Arrays.stream(type.extensions).anyMatch(lower::endsWith)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported file extension: " + fileName);
    }

    /** Validate file extension for this type */
    public boolean isValidExtension(String fileName) {
        String lower = fileName.toLowerCase();
        return Arrays.stream(this.extensions).anyMatch(lower::endsWith);
    }
}
