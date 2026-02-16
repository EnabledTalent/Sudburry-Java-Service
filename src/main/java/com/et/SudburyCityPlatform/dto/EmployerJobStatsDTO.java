package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerJobStatsDTO {

    private long jobId;
    private String role;
    private String companyName;

    private long appliedCandidates;
    private long acceptedCandidates; // HIRED/OFFERED
    private long declinedCandidates; // REJECTED
    private long requestsSent;       // REQUEST_SENT
    private long matchingCandidates; // MATCHING
}

