package com.et.SudburyCityPlatform.models.jobs;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Record of an employer inviting a job seeker (by email) to apply for a job.
 */
@Entity
@Table(name = "job_invites")
@Data
@NoArgsConstructor
public class JobInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;

    @PrePersist
    protected void onCreate() {
        if (invitedAt == null) invitedAt = LocalDateTime.now();
    }
}
