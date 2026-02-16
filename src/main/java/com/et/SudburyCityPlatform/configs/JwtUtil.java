package com.et.SudburyCityPlatform.configs;



import com.et.SudburyCityPlatform.models.jobs.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey =
                Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Parse and validate JWT claims. Throws if invalid/expired/bad signature.
     */
    public Claims parseClaims(String token) throws JwtException, IllegalArgumentException {
        return extractAllClaims(token);
    }

    public CustomUserDetails getUserFromToken(String token) {

        Claims claims = extractAllClaims(token);
        return getUserFromClaims(claims);
    }

    public CustomUserDetails getUserFromClaims(Claims claims) {

        Long userId = claimAsLong(claims, "userId");
        Long employerId = claimAsLong(claims, "employerId");
        String role = resolveRole(claims);
        String username = claims.getSubject();

        return new CustomUserDetails(
                userId,
                employerId,
                username,
                role
        );
    }

    private static Long claimAsLong(Claims claims, String key) {
        Object val = claims.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Resolve role from "role" (string) or "roles" (list); normalized to uppercase. */
    private static String resolveRole(Claims claims) {
        String role = claims.get("role", String.class);
        if (role != null && !role.isBlank()) {
            return normalizeRole(role);
        }
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles != null && !roles.isEmpty()) {
            String first = roles.get(0);
            if (first != null && !first.isBlank()) {
                return normalizeRole(first);
            }
        }
        return null;
    }

    private static String normalizeRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) return null;
        String role = rawRole.trim().toUpperCase();
        if (role.startsWith("ROLE_")) role = role.substring("ROLE_".length());
        if (role.equals("JOB_SEEKER") || role.equals("JOBSEEKER") || role.equals("SEEKER")) return "STUDENT";
        return role;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                // tolerate small clock skew between services/hosts
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

