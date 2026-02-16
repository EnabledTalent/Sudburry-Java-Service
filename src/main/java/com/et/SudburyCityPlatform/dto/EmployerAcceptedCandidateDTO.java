package com.et.SudburyCityPlatform.dto;

import com.et.SudburyCityPlatform.models.jobs.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerAcceptedCandidateDTO {
    private Long applicationId;
    private Long jobId;

    private String candidateEmail;
    private String candidateName;
    private Integer candidateYearsOfExperience;

    private String jobRole;
    private String jobLocation;

    private Integer matchPercentage; // 0..100 (computed)
    private ApplicationStatus status; // OFFERED / HIRED
}

