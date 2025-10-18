package com.project.notification_service.application.service;

import com.project.notification_service.shared.enums.Status;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = "http://localhost:4200/verify?token=" + token;

        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);

        String htmlContent = templateEngine.process("verification-email", context);
        sendHtmlMail(to, "Verify your account", htmlContent);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);

        String htmlContent = templateEngine.process("reset-password-email", context);
        sendHtmlMail(to, "Password Reset Request", htmlContent);
    }

    public void notifyStaffAboutNewIssue(String title, String description, String issueId, List<String> staffEmails) {
        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("description", description);
        context.setVariable("issueId", issueId);
        context.setVariable("issueUrl", "http://localhost:4200/staff/issues/" + issueId);

        String htmlContent = templateEngine.process("new-issue-email", context);

        staffEmails.forEach(email ->
                sendHtmlMail(email, "New Issue Created: " + title, htmlContent)
        );
    }

    public void notifyCitizenAboutStatusChange(String citizenEmail, String issueId, Status newStatus) {
        Context context = new Context();
        context.setVariable("issueId", issueId);
        context.setVariable("newStatus", newStatus.name());
        context.setVariable("issueUrl", "http://localhost:4200/citizen/issues/" + issueId);

        String htmlContent = templateEngine.process("issue-status-change-email", context);

        sendHtmlMail(citizenEmail, "Issue Status Updated", htmlContent);
    }

    private void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
