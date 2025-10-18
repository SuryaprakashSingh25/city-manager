package com.project.notification_service.application.service;

import com.project.notification_service.shared.enums.Status;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendVerificationEmail_ShouldProcessTemplateAndSend() {
        when(templateEngine.process(eq("verification-email"), any(Context.class)))
                .thenReturn("<html>verify</html>");

        mailService.sendVerificationEmail("user@example.com", "token123");

        verify(templateEngine, times(1))
                .process(eq("verification-email"), any(Context.class));

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendPasswordResetEmail_ShouldProcessTemplateAndSend() {
        when(templateEngine.process(eq("reset-password-email"), any(Context.class)))
                .thenReturn("<html>reset</html>");

        mailService.sendPasswordResetEmail("user@example.com", "token456");

        verify(templateEngine, times(1))
                .process(eq("reset-password-email"), any(Context.class));

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testNotifyStaffAboutNewIssue_ShouldSendToAllStaff() {
        when(templateEngine.process(eq("new-issue-email"), any(Context.class)))
                .thenReturn("<html>issue</html>");

        mailService.notifyStaffAboutNewIssue(
                "Broken Streetlight",
                "Light not working on 5th Ave",
                "ISSUE123",
                java.util.List.of("s1@example.com", "s2@example.com")
        );

        verify(templateEngine, times(1))
                .process(eq("new-issue-email"), any(Context.class));

        // Should send twice (once per staff)
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    void testNotifyCitizenAboutStatusChange_ShouldSendToCitizen() {
        when(templateEngine.process(eq("issue-status-change-email"), any(Context.class)))
                .thenReturn("<html>status</html>");

        mailService.notifyCitizenAboutStatusChange(
                "citizen@example.com",
                "ISSUE789",
                Status.RESOLVED
        );

        verify(templateEngine, times(1))
                .process(eq("issue-status-change-email"), any(Context.class));

        verify(mailSender, times(1)).send(mimeMessage);
    }

}
