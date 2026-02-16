package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerDashboardMetricsDTO {

    /**
     * Time window used for this response (days).
     * Suggested values: 7, 30, 90, 365
     */
    private Integer windowDays;

    // Cards (right side in UI)
    private long matchingApplicants;
    private long activeJobs;
    private double avgApplicantsPerJob;

    private long candidatesAccepted;          // OFFERED + HIRED
    private Integer acceptedChangePct;        // compared to previous window (null if not computable)
    private Integer activeJobsChangePct;      // compared to previous window (null if not computable)

    // Chart
    private List<AcceptanceRatePoint> acceptanceRateSeries;
    private List<AcceptanceRatePoint> projectedAcceptanceRateSeries;

    // Attention Needed
    private AttentionNeeded attentionNeeded;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcceptanceRatePoint {
        private LocalDate date;
        private int acceptanceRatePct; // 0..100
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttentionNeeded {
        private long candidatesPendingReview; // UNDER_REVIEW
        private long jobsNearingDeadline;     // heuristic
        private long lowMatchJobs;            // heuristic
        private List<String> notes;           // ready-to-render bullet list
    }
}

