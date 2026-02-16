package com.et.SudburyCityPlatform.dto;

import com.et.SudburyCityPlatform.models.jobs.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One recruiter-action notification for the job seeker.
 * On click, use jobId to show which job it relates to.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerNotificationDTO {

    private Long applicationId;
    private Long jobId;

    private String jobRole;
    private String companyName;

    private ApplicationStatus status;
    private String message;

    private LocalDateTime timestamp;
}
