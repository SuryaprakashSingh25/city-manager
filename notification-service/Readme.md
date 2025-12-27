# Notification Service

The **Notification Service** is responsible for sending emails and system notifications.
It operates **asynchronously** and **does not expose any REST APIs**.

This service reacts to events published by other services and handles notification delivery
without blocking core business workflows.

---

## Responsibilities

- Send email verification links after user registration
- Send password reset emails
- Notify users when:
  - An issue is created
  - An issue’s status is changed or assigned
- Consume Kafka events and process them asynchronously

---


## Kafka Topics Consumed

| Topic | Description |
|-----|-------------|
| `user-registered` | Triggered after a user registers |
| `password-reset` | Triggered on password reset request |
| `issue-create` | Triggered when a new issue is created |
| `issue-status-change` | Triggered when an issue status changes |

---

## Kafka Event Listeners

### User & Authentication Events

```java
@Component
@RequiredArgsConstructor
public class MailEventListener {

    private final MailService mailService;

    @KafkaListener(topics = "user-registered", groupId = "notification-group")
    public void handleUserRegistered(UserRegisteredEvent event) {
        mailService.sendVerificationEmail(
            event.getEmail(),
            event.getVerificationToken()
        );
    }

    @KafkaListener(topics = "password-reset", groupId = "notification-group")
    public void handlePasswordReset(PasswordResetRequestedEvent event) {
        mailService.sendPasswordResetEmail(
            event.getEmail(),
            event.getResetToken()
        );
    }
}
```

### Issue Events
```java
@Component
@RequiredArgsConstructor
public class IssueEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "issue-create", groupId = "notification-group")
    public void handleIssueCreated(IssueCreatedEvent event) {
        notificationService.handleIssueCreated(event);
    }

    @KafkaListener(topics = "issue-status-change", groupId = "notification-group")
    public void handleIssueStatusChanged(IssueStatusChangedEvent event) {
        notificationService.handleIssueAssignedOrUpdated(event);
    }
}
```

### Email Configuration
- Email delivery is configured using Gmail SMTP.
- Use a Gmail App Password instead of your real password.
  
```bash
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_NAME}
    password: ${GMAIL_PASS}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

```
