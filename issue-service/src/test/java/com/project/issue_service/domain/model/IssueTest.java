package com.project.issue_service.domain.model;

import com.project.issue_service.domain.enums.Status;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IssueTest {

    @Test
    void createDraft_ShouldInitializeCorrectly() {
        Issue issue = Issue.createDraft("Title", "Desc", "Loc", "user-1");

        assertEquals(Status.DRAFT, issue.getStatus());
        assertEquals("Title", issue.getTitle());
        assertTrue(issue.getAttachments().isEmpty());
        assertNotNull(issue.getCreatedAt());
        assertNotNull(issue.getUpdatedAt());
    }

    @Test
    void submit_ShouldChangeStatusToOpen() {
        Issue draft = Issue.createDraft("T", "D", "L", "u");
        Issue submitted = draft.submit();

        assertEquals(Status.OPEN, submitted.getStatus());
        assertEquals(draft.getId(), submitted.getId());
        assertTrue(!submitted.getUpdatedAt().isBefore(draft.getUpdatedAt()));
    }

    @Test
    void submit_ShouldThrow_WhenNotDraft() {
        Issue issue = Issue.createDraft("T", "D", "L", "u").submit();
        assertThrows(IllegalStateException.class, issue::submit);
    }

    @Test
    void accept_ShouldChangeStatusToInProgress() {
        Issue open = Issue.createDraft("T", "D", "L", "u").submit();
        Issue accepted = open.accept("staff-1");

        assertEquals(Status.IN_PROGRESS, accepted.getStatus());
        assertEquals("staff-1", accepted.getAssignedStaffId());
    }

    @Test
    void accept_ShouldThrow_WhenNotOpen() {
        Issue draft = Issue.createDraft("T", "D", "L", "u");
        assertThrows(IllegalStateException.class, () -> draft.accept("s"));
    }

    @Test
    void updateStatus_ShouldChangeStatusAndAddComment() {
        Issue inProgress = Issue.createDraft("T", "D", "L", "u").submit().accept("s1");
        Issue resolved = inProgress.updateStatus(Status.RESOLVED, "done");

        assertEquals(Status.RESOLVED, resolved.getStatus());
        assertEquals("done", resolved.getStaffComment());
    }

    @Test
    void updateStatus_ShouldThrow_WhenClosedIssue() {
        Issue resolved = Issue.createDraft("T", "D", "L", "u")
                .submit()
                .accept("s1")
                .updateStatus(Status.RESOLVED, "done");

        assertThrows(IllegalStateException.class,
                () -> resolved.updateStatus(Status.OPEN, "again"));
    }

    @Test
    void updateAttachments_ShouldReplaceAttachments() {
        Issue issue = Issue.createDraft("T", "D", "L", "u").submit();
        List<AttachmentRef> newAttachments = List.of(new AttachmentRef("m1"));

        Issue updated = issue.updateAttachments(newAttachments);

        assertEquals(1, updated.getAttachments().size());
        assertEquals("m1", updated.getAttachments().get(0).mediaId());
    }
}
