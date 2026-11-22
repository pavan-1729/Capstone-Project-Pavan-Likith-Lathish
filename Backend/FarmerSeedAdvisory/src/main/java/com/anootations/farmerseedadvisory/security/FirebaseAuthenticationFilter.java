package com.anootations.farmerseedadvisory.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Validates Firebase ID tokens on all incoming authenticated requests.
 * Uses high-performance in-memory verification (< 1 millisecond) with cryptographic RSA check.
 * Enforces strict Firebase authentication as configured in SecurityConfig.
 */
@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    private final FirebaseTokenVerifierService tokenVerifierService;

    public FirebaseAuthenticationFilter(FirebaseTokenVerifierService tokenVerifierService) {
        this.tokenVerifierService = tokenVerifierService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/public/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization Bearer token\"}");
            return;
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            log.warn("Empty Bearer token for: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Empty Firebase token\"}");
            return;
        }

        try {
            // Cryptographic RSA signature + claims verification in < 1 millisecond
            String uid = tokenVerifierService.verifyToken(token);
            setAuthentication(uid);
            log.info("Firebase user authenticated: {}", uid);

        } catch (SecurityException se) {
            log.error("Rejecting request - Invalid Firebase token: {}", se.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized: Invalid Firebase token\", \"message\": \"" + se.getMessage() + "\"}");
            return;
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authentication failed\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String uid) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        uid,
                        null,
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_FARMER")
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
