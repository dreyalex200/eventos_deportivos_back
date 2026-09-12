package com.drey.eventsSports.shared.utils;

import com.drey.eventsSports.shared.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final String secret;
    private final long expirationSeconds;
    private final SecretKey key;

    public JwtUtils(
            @Value("${app.security.jwt.secret:" + SecurityConstants.JWT_SECRET_DEFAULT + "}") String secret,
            @Value("${app.security.jwt.expiration:86400}") long expirationSeconds) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            this.key = Keys.hmacShaKeyFor(padded);
        } else {
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String generateToken(Long userId, String email, String username, List<String> roles) {
        return generateToken(userId, email, username, roles, this.expirationSeconds);
    }

    public String generateToken(Long userId, String email, String username, List<String> roles, long customExpirationSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (customExpirationSeconds * 1000));

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("username", username);
        claims.put("roles", roles);
        if (roles != null && !roles.isEmpty()) {
            claims.put(SecurityConstants.CLAIM_ROLE, roles.get(0));
        }
        claims.put(SecurityConstants.CLAIM_SCOPE_ID, UUID.randomUUID().toString());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email", String.class);
    }

    public String getUserNameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String username = claims.get("username", String.class);
        return username != null ? username : claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof List<?>) {
            return ((List<?>) rolesObj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        String singleRole = claims.get(SecurityConstants.CLAIM_ROLE, String.class);
        if (singleRole != null) {
            return List.of(singleRole);
        }
        return Collections.emptyList();
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        List<String> roles = getRolesFromToken(token);
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
            if (!role.startsWith("ROLE_")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        return authorities;
    }

    public UUID getScopeIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String scopeId = claims.get(SecurityConstants.CLAIM_SCOPE_ID, String.class);
            if (scopeId != null) {
                return UUID.fromString(scopeId);
            }
        } catch (Exception ignored) {
        }
        return UUID.randomUUID();
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
