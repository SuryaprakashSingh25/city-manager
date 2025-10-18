package com.project.user_service.infrastructure.repository;

import com.project.user_service.domain.model.User;

public class UserMapper {
    public static UserDocument toDocument(User user){
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setEmail(user.getEmail());
        doc.setPasswordHash(user.getPasswordHash());
        doc.setRole(user.getRole());
        doc.setStatus(user.getStatus());
        doc.setCreatedAt(user.getCreatedAt());
        doc.setVerificationToken(user.getVerificationToken());
        doc.setVerificationTokenExpiry(user.getVerificationTokenExpiry());
        doc.setResetToken(user.getResetToken());
        doc.setResetTokenExpiry(user.getResetTokenExpiry());
        return doc;
    }

    public static User toDomain(UserDocument doc){
        User user=new User(
                doc.getEmail(),
                doc.getPasswordHash(),
                doc.getRole()
        );

        try{
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, doc.getId());

            var createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, doc.getCreatedAt());

            var statusField = User.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(user, doc.getStatus());

            var verificationTokenField = User.class.getDeclaredField("verificationToken");
            verificationTokenField.setAccessible(true);
            verificationTokenField.set(user, doc.getVerificationToken());

            var verificationTokenExpiryField = User.class.getDeclaredField("verificationTokenExpiry");
            verificationTokenExpiryField.setAccessible(true);
            verificationTokenExpiryField.set(user, doc.getVerificationTokenExpiry());

            var resetTokenField = User.class.getDeclaredField("resetToken");
            resetTokenField.setAccessible(true);
            resetTokenField.set(user, doc.getResetToken());

            var resetTokenExpiryField = User.class.getDeclaredField("resetTokenExpiry");
            resetTokenExpiryField.setAccessible(true);
            resetTokenExpiryField.set(user, doc.getResetTokenExpiry());

        } catch (Exception e) {
            throw new RuntimeException("Error mapping UserDocument to User", e);
        }
        return user;
    }

}
