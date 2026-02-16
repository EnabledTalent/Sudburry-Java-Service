package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.dto.JobSeekerMetricsDTO;
import com.et.SudburyCityPlatform.service.Jobs.JobSeekerMetricsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs/jobseeker")
@PreAuthorize("hasRole('STUDENT')")
public class JobSeekerMetricsController {

    private final JobSeekerMetricsService jobSeekerMetricsService;

    public JobSeekerMetricsController(JobSeekerMetricsService jobSeekerMetricsService) {
        this.jobSeekerMetricsService = jobSeekerMetricsService;
    }

    /**
     * Metrics for a job seeker.
     * - If email is not provided, uses auth.getName() (JWT subject).
     * - windowDays defaults to 90 (3 months).
     */
    @GetMapping("/metrics")
    public JobSeekerMetricsDTO metrics(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer windowDays,
            Authentication auth
    ) {
        String resolved = (email != null && !email.isBlank())
                ? email
                : (auth != null ? auth.getName() : null);
        return jobSeekerMetricsService.metrics(resolved, windowDays);
    }
}

