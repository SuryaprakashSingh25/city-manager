package com.project.notification_service.infrastructure.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignClientInterceptor implements RequestInterceptor {
    private final ServiceJwtProvider jwtProvider;

    @Override
    public void apply(RequestTemplate template) {
        String token = "Bearer " + jwtProvider.generateToken("notification-service");
        template.header("Authorization", token);
    }
}
