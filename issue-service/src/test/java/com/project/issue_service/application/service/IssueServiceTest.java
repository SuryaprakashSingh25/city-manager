package com.project.issue_service.application.service;

import com.project.issue_service.application.event.IssueEventPublisher;
import com.project.issue_service.domain.enums.Status;
import com.project.issue_service.domain.model.AttachmentRef;
import com.project.issue_service.domain.model.Issue;
import com.project.issue_service.domain.model.IssueId;
import com.project.issue_service.domain.repository.IssueRepository;
import com.project.issue_service.exposition.dto.PagedResponse;
import com.project.issue_service.infrastructure.redis.RedisLockService;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private RedisLockService redisLockService;

    @Mock
    private IssueEventPublisher issueEventPublisher;

    @InjectMocks
    private IssueService issueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------- CREATE ----------
    @Test
    void createIssue_ShouldSaveDraftIssue() {
        Issue draft = Issue.createDraft("Title", "Desc", "Loc", "user1");
        when(issueRepository.save(any(Issue.class))).thenReturn(draft);

        Issue result = issueService.createIssue("Title", "Desc", "Loc", "user1");

        assertNotNull(result);
        assertEquals(Status.DRAFT, result.getStatus());
        verify(issueRepository).save(any(Issue.class));
    }

    // ---------- SUBMIT ----------
    @Test
    void submitIssue_ShouldPublishAndSave() {
        Issue draft = Issue.createDraft("Title", "Desc", "Loc", "u1");
        when(issueRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
        when(issueRepository.save(any(Issue.class))).thenAnswer(inv -> inv.getArgument(0));

        Issue submitted = issueService.submitIssue(draft.getId().getValue(), "u1");

        assertEquals(Status.OPEN, submitted.getStatus());
        verify(issueRepository).save(any(Issue.class));
        verify(issueEventPublisher).publishIssueCreated(any(Issue.class));
    }

    @Test
    void submitIssue_ShouldThrow_WhenUserMismatch() {
        Issue draft = Issue.createDraft("Title", "Desc", "Loc", "userA");
        when(issueRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThrows(IllegalStateException.class,
                () -> issueService.submitIssue(draft.getId().getValue(), "userB"));
    }

    // ---------- GET OPEN ISSUE DETAILS ----------
    @Test
    void getOpenIssueDetails_ShouldAcquireLock() {
        Issue issue = Issue.createDraft("T", "D", "L", "c1").submit();
        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(redisLockService.acquireLock(issue.getId().getValue(), "s1", 180)).thenReturn(true);

        Issue result = issueService.getOpenIssueDetails(issue.getId().getValue(), "s1");

        assertEquals(issue, result);
        verify(redisLockService).acquireLock(issue.getId().getValue(), "s1", 180);
    }

    @Test
    void getOpenIssueDetails_ShouldThrow_WhenLocked() {
        Issue issue = Issue.createDraft("T", "D", "L", "c1").submit();
        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
        when(redisLockService.acquireLock(any(), any(), anyInt())).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> issueService.getOpenIssueDetails(issue.getId().getValue(), "s2"));
    }

    @Test
    void getOpenIssueDetails_ShouldThrow_WhenNotFound() {
        when(issueRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> issueService.getOpenIssueDetails("missing", "s1"));
    }

    // ---------- ACCEPT ----------
    @Test
    void acceptIssue_ShouldChangeToInProgress_AndPublishEvent() {
        Issue open = Issue.createDraft("T", "D", "L", "u1").submit();
        when(issueRepository.findById(open.getId())).thenReturn(Optional.of(open));
        when(issueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Issue result = issueService.acceptIssue("staff1", open.getId().getValue());

        assertEquals(Status.IN_PROGRESS, result.getStatus());
        assertEquals("staff1", result.getAssignedStaffId());
        verify(issueEventPublisher).publishIssueStatusChanged(any());
    }

    @Test
    void acceptIssue_ShouldThrow_WhenNotOpen() {
        Issue nonOpen = Issue.createDraft("T", "D", "L", "u1");
        when(issueRepository.findById(nonOpen.getId())).thenReturn(Optional.of(nonOpen));

        assertThrows(IllegalStateException.class,
                () -> issueService.acceptIssue("s1", nonOpen.getId().getValue()));
    }

    // ---------- UPDATE STATUS ----------
    @Test
    void updateIssueStatus_ShouldSaveAndPublish() {
        Issue open = Issue.createDraft("T", "D", "L", "u1").submit().accept("s1");
        when(issueRepository.findById(open.getId())).thenReturn(Optional.of(open));
        when(issueRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Issue updated = issueService.updateIssueStatus("s1", open.getId().getValue(), Status.RESOLVED, "done");

        assertEquals(Status.RESOLVED, updated.getStatus());
        assertEquals("done", updated.getStaffComment());
        verify(issueRepository).save(any());
        verify(issueEventPublisher).publishIssueStatusChanged(any());
    }

    @Test
    void updateIssueStatus_ShouldThrow_WhenUnauthorized() {
        Issue open = Issue.createDraft("T", "D", "L", "u1").submit().accept("staff1");
        when(issueRepository.findById(open.getId())).thenReturn(Optional.of(open));

        assertThrows(IllegalStateException.class,
                () -> issueService.updateIssueStatus("staff2", open.getId().getValue(), Status.RESOLVED, "done"));
    }

    // ---------- ATTACHMENT ----------
    @Test
    void addAttachment_ShouldUpdateIssue() {
        Issue issue = Issue.createDraft("T", "D", "L", "u1");
        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

        issueService.addAttachment(issue.getId(), new AttachmentRef("m1"));

        verify(issueRepository).save(any(Issue.class));
    }

    @Test
    void removeAttachment_ShouldFilterAndSave() {
        AttachmentRef ref1 = new AttachmentRef("m1");
        AttachmentRef ref2 = new AttachmentRef("m2");
        Issue issue = issue = Issue.createDraft("T", "D", "L", "u1")
                .updateAttachments(List.of(ref1, ref2));
        when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

        issueService.removeAttachment(issue.getId(), "m1");

        verify(issueRepository).save(any(Issue.class));
    }

    // ---------- LOCK ----------
    @Test
    void isLocked_ShouldReturnTrueWhenHolderExists() {
        when(redisLockService.getLockHolder("id1")).thenReturn("staff1");
        assertTrue(issueService.isLocked("id1"));
    }

    @Test
    void releaseIssueLock_ShouldInvokeRedisService() {
        issueService.releaseIssueLock("staff1", "id1");
        verify(redisLockService).releaseLock("id1", "staff1");
    }
}
