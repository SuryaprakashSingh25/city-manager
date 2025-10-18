package com.project.notification_service.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class ServiceJwtProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}") // 10 minutes
    private long expirationMs;

    public String generateToken(String serviceId) {
        Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
        return Jwts.builder()
                .setSubject(serviceId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .claim("role", "SERVICE")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
