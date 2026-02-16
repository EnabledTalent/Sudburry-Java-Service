package com.et.SudburyCityPlatform.models.jobs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employer_org_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerOrganizationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String organizationName;

    @Column(columnDefinition = "TEXT")
    private String aboutOrganization;

    private String location;

    private Integer foundedYear;

    private String website;

    /**
     * e.g. "1-10", "10-100", "100-1000", "1000-10000"
     */
    private String companySize;

    /**
     * e.g. "Information Technology"
     */
    private String industry;

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

