package com.project.user_service.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public static final List<String> PUBLIC_PATHS = List.of(
            "/api/users/register",
            "/api/users/login",
            "/api/users/verify",
            "/api/users/forgot-password",
            "/api/users/reset-password",
            "/api/users/refresh-token",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String userId;

        try {
            userId = jwtUtil.extractUserId(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwt)) {
                String role = jwtUtil.extractRole(jwt);

                if ("SERVICE".equals(role)) {
                    // Authenticate as a service token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId, null, List.of(() -> "ROLE_SERVICE")
                            );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Regular user token
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

