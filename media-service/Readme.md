# Media Service

The **Media Service** manages uploading, downloading, and retrieving media files
(images, documents) associated with issues in the City Manager system.  
It enforces **role-based access** and integrates with the Issue service.

---


## 1. Overview

- Service Name: `media-service`  
- Base URL: `/api/media`  
- Handles media related to issues:
  - Upload
  - Download
  - List by issue
  - Delete
- Integrates with Issue Service via Feign Client
- Enforces user-level access:
  - Citizens can only manage their own uploads
  - Staff can access all media

---

## 2. Responsibilities

- Upload media to an issue
- Media files are stored in a **private AWS S3 bucket**.
- Downloads and issue media viewing generate **presigned URLs**:
  - Citizens can view or download their own media.
  - Staff can view or download any issue’s media without downloading files.
- Presigned URLs provide temporary, secure access to private media.
- Delete media files
- Enforce access permissions based on user role

---

## 3. Security & Roles

- **CITIZEN**
  - Upload media for issues
  - View & download their own media
  - Delete their own media
- **STAFF**
  - View & download all media for any issue
- All endpoints secured via `@PreAuthorize`
- Authenticated user info retrieved from Spring Security context

---

## 4. API Endpoints

| Endpoint | Method | Description | Roles |
|---------|--------|------------|-------|
| `/upload/{issueId}` | POST | Upload a media file for an issue | CITIZEN |
| `/issues/{issueId}` | GET | List media for a specific issue | CITIZEN, STAFF |
| `/{mediaId}` | GET | Get media metadata by ID | CITIZEN, STAFF |
| `/download/{mediaId}` | GET | Download media content | CITIZEN, STAFF |
| `/{mediaId}` | DELETE | Delete a media file | CITIZEN |

**Notes:**
- Citizens can only access media they uploaded
- Staff can access all media

---

## 5. How It Works

1. **Upload Media**
   - Citizens POST a file to `/upload/{issueId}`
   - Media is saved and associated with the issue and user
2. **List Media**
   - `/issues/{issueId}` returns media for the issue
   - Citizens only see their own uploads
   - Staff see all media
3. **Retrieve Media**
   - `/media/{mediaId}` returns metadata
   - Access controlled based on user role
4. **Download Media**
   - `/download/{mediaId}` returns file content
   - Content type: `image/jpeg`
   - Attachment filename preserved
5. **Delete Media**
   - Citizens can delete only their own uploads

---

## Implementation Notes

- Uses **Spring Security** for authentication and role enforcement
- Uses **Feign Client** to communicate with Issue service
- Supports **ByteArrayResource** for file downloads
- Ensures access control checks on every media retrieval or deletion
- DTO mapping handled by `MediaDTOMapper`


