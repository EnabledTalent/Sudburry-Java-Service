package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Invite notification for a job seeker: employer invited them to apply for a job.
 * On click, use jobId to show which job.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerInviteDTO {

    private Long inviteId;
    private Long jobId;

    private String jobRole;
    private String companyName;

    private String message;
    private LocalDateTime invitedAt;
}
