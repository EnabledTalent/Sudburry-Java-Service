package com.et.SudburyCityPlatform.models.serviceprovider;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_provider_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owner identity - from authenticated user (SERVICEPROVIDER role).
     * Multiple profiles can share the same email.
     */
    @Column(nullable = false)
    private String email;

    @Column(name = "provider_type", columnDefinition = "TEXT")
    private String providerType;

    @Column(name = "organization_name", nullable = false, columnDefinition = "TEXT")
    private String organizationName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "programs_and_services", columnDefinition = "TEXT")
    private String programsAndServices;

    @Column(name = "contact_preferences", columnDefinition = "TEXT")
    private String contactPreferences;

    @Column(name = "impact_reporting_preferences", columnDefinition = "TEXT")
    private String impactReportingPreferences;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String website;

    @Column(columnDefinition = "TEXT")
    private String phone;

    @Column(name = "contact_email", columnDefinition = "TEXT")
    private String contactEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
