package com.et.SudburyCityPlatform.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null || token.isEmpty()) {
            request.setAttribute("jwt_error", "missing_token");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var claims = jwtUtil.parseClaims(token);
            var user = jwtUtil.getUserFromClaims(claims);

            String role = normalizeRole(user.getRole());

            Authentication auth =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            request.setAttribute("jwt_error", e.getClass().getSimpleName());
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Normalize role values coming from different token issuers.
     * Examples handled:
     * - "student" -> "STUDENT"
     * - "ROLE_STUDENT" -> "STUDENT"
     * - "JOB_SEEKER" / "JOBSEEKER" -> "STUDENT"
     */
    private static String normalizeRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) return "USER";
        String role = rawRole.trim().toUpperCase();
        if (role.startsWith("ROLE_")) role = role.substring("ROLE_".length());
        if (role.equals("JOB_SEEKER") || role.equals("JOBSEEKER") || role.equals("SEEKER")) return "STUDENT";
        return role;
    }

    private static String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && !header.isBlank()) {
            String h = header.trim();
            // case-insensitive "Bearer " check
            if (h.length() >= 7 && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return h.substring(7).trim();
            }
            // Some clients send the raw JWT in Authorization without the Bearer prefix
            if (looksLikeJwt(h)) {
                return h;
            }
        }

        // Common alternative headers
        String alt = firstNonBlank(
                request.getHeader("X-Auth-Token"),
                request.getHeader("X-Access-Token"),
                request.getHeader("x-access-token")
        );
        if (alt != null) return alt.trim();

        // Cookie fallback (common in browser apps)
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c == null) continue;
                String name = c.getName();
                if (name == null) continue;
                if (name.equalsIgnoreCase("token")
                        || name.equalsIgnoreCase("jwt")
                        || name.equalsIgnoreCase("access_token")) {
                    String v = c.getValue();
                    if (v != null && !v.isBlank()) return v.trim();
                }
            }
        }

        return null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    private static boolean looksLikeJwt(String value) {
        // very small heuristic: header.payload.signature (2 dots)
        int first = value.indexOf('.');
        if (first <= 0) return false;
        int second = value.indexOf('.', first + 1);
        return second > first + 1 && second < value.length() - 1;
    }
}



