package com.project.user_service.infrastructure.config;

import com.project.user_service.domain.model.Role;
import com.project.user_service.domain.model.User;
import com.project.user_service.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if(!userRepository.existsByRole(Role.ADMIN)){
            User admin=new User(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    Role.ADMIN
            );
            admin.activate(admin.getVerificationToken());
            userRepository.save(admin);
            System.out.println("✅ Bootstrapped Admin: admin@system.com");
        }
    }
}
