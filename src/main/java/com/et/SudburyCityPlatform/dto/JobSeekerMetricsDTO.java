package com.et.SudburyCityPlatform.dto;

import com.et.SudburyCityPlatform.models.jobs.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerMetricsDTO {
    private String email;

    // Overall
    private long totalApplied;
    private Map<ApplicationStatus, Long> totalByStatus;

    // Last N days window
    private int windowDays;
    private LocalDateTime windowStart;
    private long appliedInWindow;
    private Map<ApplicationStatus, Long> byStatusInWindow;
}

