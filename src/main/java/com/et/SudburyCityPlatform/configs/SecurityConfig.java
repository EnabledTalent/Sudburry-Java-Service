package com.et.SudburyCityPlatform.configs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return  http.cors() // 👈 IMPORTANT
                .and()
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Always allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public root + health endpoints (useful for Render / browser checks)
                        .requestMatchers("/", "/health", "/error").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // These APIs are protected via @PreAuthorize and require a valid JWT.
                        .requestMatchers(
                                "/api/v1/jobs/**",
                                "/api/jobseeker/**",
                                "/api/employer/**",
                                "/api/auth/**"
                        ).authenticated()
                        // Public APIs (programs, events, etc.)
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            Object jwtError = request.getAttribute("jwt_error");
                            String msg = jwtError != null && !jwtError.toString().isBlank()
                                    ? ("Unauthorized (" + jwtError + ")")
                                    : "Unauthorized";
                            response.getWriter().write(
                                    "{\"message\":\"" + msg + "\",\"status\":401,\"error\":\"Unauthorized\",\"timestamp\":\""
                                            + Instant.now()
                                            + "\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\":\"Access Denied\",\"status\":403,\"error\":\"Forbidden\",\"timestamp\":\""
                                            + Instant.now()
                                            + "\"}"
                            );
                        })
                )
                // 🔥 THIS LINE MAKES YOUR FILTER RUN
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config =
                new org.springframework.web.cors.CorsConfiguration();

        config.setAllowedOrigins(
                java.util.List.of(
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://localhost:8083",
                        "http://127.0.0.1:8080",
                        "http://127.0.0.1:8083",
                        "http://127.0.0.1:3000",
                        "https://sudbury-city-ui.vercel.app",
                        "https://java-services-sudburry.onrender.com"
                )
        );
        config.setAllowedMethods(
                java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

