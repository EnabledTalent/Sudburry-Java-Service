package com.et.SudburyCityPlatform.models.auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "user_login_state",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_login_state_email", columnNames = "email")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    /**
     * Set to true after the first successful login check.
     * We derive "firstTimeLogin" as (!hasLoggedInBefore).
     */
    @Column(name = "has_logged_in_before", nullable = false)
    private boolean hasLoggedInBefore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

