package com.drey.eventsSports.infrastructure.config;

import com.drey.eventsSports.shared.constants.SecurityConstants;
import com.drey.eventsSports.shared.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Value("${app.security.app-token:}")
    private String appToken;

    @Value("${app.security.app-token-header:X-App-Token}")
    private String appTokenHeader;

    @Value("${app.security.app-scope-header:X-Scope-Id}")
    private String appScopeHeader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(SecurityConstants.HEADER_STRING);
        
        if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            String token = header.substring(SecurityConstants.TOKEN_PREFIX.length());

            if (!jwtUtils.validateToken(token)) {
                throw new BadCredentialsException("Invalid JWT token");
            }

            String userId = jwtUtils.getUserIdFromToken(token);
            String email = jwtUtils.getEmailFromToken(token);
            String principal = email != null ? email : userId;
            java.util.List<GrantedAuthority> authorities = jwtUtils.getAuthoritiesFromToken(token);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("user_id", userId);
            request.setAttribute("email", email);
            request.setAttribute("auth_method", "jwt");
            UUID scopeId = jwtUtils.getScopeIdFromToken(token);
            if (scopeId != null) {
                request.setAttribute("scope_id", scopeId);
            }

            filterChain.doFilter(request, response);
            return;
        }

        String appHeader = request.getHeader(appTokenHeader);
        if (appHeader != null && !appHeader.isBlank() && appToken != null && !appToken.isBlank() && safeEquals(appHeader, appToken)) {
            String scopeHeader = request.getHeader(appScopeHeader);
            if (scopeHeader == null || scopeHeader.isBlank()) {
                throw new BadCredentialsException("Missing required header: " + appScopeHeader);
            }

            UUID scopeId;
            try {
                scopeId = UUID.fromString(scopeHeader);
            } catch (IllegalArgumentException ex) {
                throw new BadCredentialsException("Invalid UUID in header: " + appScopeHeader);
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    scopeId.toString(), null, Collections.emptyList());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("scope_id", scopeId);
            request.setAttribute("auth_method", "app_token");

            filterChain.doFilter(request, response);
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean safeEquals(String a, String b) {
        byte[] aBytes = a == null ? new byte[0] : a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b == null ? new byte[0] : b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }
}
