package com.integrityshield.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        System.out.println("\n================ JWT FILTER =================");
        System.out.println("➡️ Request Path: " + path);

        // ✅ Skip login
        if (path.equals("/api/student/login")) {
            System.out.println("⏭️ Skipping login endpoint");
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        System.out.println("📥 Authorization Header: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("❌ No valid Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        System.out.println("🔑 Extracted Token: " + token);

        boolean valid = jwtUtil.validateToken(token);
        System.out.println("✅ Token valid: " + valid);

        if (!valid) {
            System.out.println("❌ Invalid token");
            filterChain.doFilter(request, response);
            return;
        }

        String userId = jwtUtil.extractUserIdentifier(token);
        String role = jwtUtil.extractRole(token);

        System.out.println("👤 User: " + userId);
        System.out.println("🎭 Role from token: " + role);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("✅ Authentication set in SecurityContext");
        System.out.println("============================================\n");

        filterChain.doFilter(request, response);
    }
}