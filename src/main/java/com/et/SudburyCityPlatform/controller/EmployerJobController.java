package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.dto.EmployerAcceptedCandidateDTO;
import com.et.SudburyCityPlatform.dto.EmployerDashboardMetricsDTO;
import com.et.SudburyCityPlatform.dto.EmployerJobPostRequestDTO;
import com.et.SudburyCityPlatform.dto.EmployerJobStatsDTO;
import com.et.SudburyCityPlatform.models.jobs.*;
import com.et.SudburyCityPlatform.repository.Jobs.EmployerRepository;
import com.et.SudburyCityPlatform.service.Jobs.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Employer-facing job endpoints.
 * Class-level auth as requested: ADMIN or EMPLOYER.
 */
@RestController
@RequestMapping("/api/v1/jobs/employer")
@PreAuthorize("hasAnyRole('ADMIN','EMPLOYER')")
public class EmployerJobController {

    private final JobService jobService;
    private final EmployerRepository employerRepository;

    public EmployerJobController(JobService jobService, EmployerRepository employerRepository) {
        this.jobService = jobService;
        this.employerRepository = employerRepository;
    }

    // ---- Employer Jobs CRUD ----

    @GetMapping("/jobs")
    public List<Job> getEmployerJobs(
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.getJobsByEmployer(employerId);
    }

    @GetMapping("/jobs/stats")
    public List<EmployerJobStatsDTO> getEmployerJobsWithStats(
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.getEmployerJobsWithStats(employerId);
    }

    /**
     * Employer dashboard: candidates that were accepted for any of this employer's jobs.
     * Accepted = OFFERED or HIRED.
     */
    @GetMapping("/candidates/accepted")
    public List<EmployerAcceptedCandidateDTO> acceptedCandidates(
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.getAcceptedCandidatesForEmployer(employerId);
    }

    /**
     * Employer dashboard small metrics (for the dashboard image cards + chart).
     * windowDays: 7, 30, 90, 365
     */
    @GetMapping("/metrics")
    public EmployerDashboardMetricsDTO dashboardMetrics(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer windowDays,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.getEmployerDashboardMetrics(employerId, windowDays);
    }

    @PostMapping("/jobs")
    public ResponseEntity<Job> createEmployerJob(
            @Valid @RequestBody EmployerJobPostRequestDTO request,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        Job created = jobService.createJobForEmployer(employerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/jobs/{jobId}")
    public Job getEmployerJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.getJobForEmployer(employerId, jobId);
    }

    @PutMapping("/jobs/{jobId}")
    public Job updateEmployerJob(
            @PathVariable Long jobId,
            @Valid @RequestBody EmployerJobPostRequestDTO request,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.updateJobForEmployer(employerId, jobId, request);
    }

    @DeleteMapping("/jobs/{jobId}")
    public void deleteEmployerJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        jobService.deleteJobForEmployer(employerId, jobId);
    }

    /**
     * Invite a job seeker (by email) to apply for this job. Sends an invite email and records the invite.
     */
    @PostMapping("/jobs/{jobId}/invite")
    public JobInvite inviteToApply(
            @PathVariable Long jobId,
            @RequestParam String inviteeEmail,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.inviteToApply(jobId, inviteeEmail.trim(), employerId);
    }

    // ---- Employer Applicants ----

    @GetMapping("/jobs/{jobId}/applications")
    public List<JobApplicationRequest> getApplicantsForJob(
            @PathVariable Long jobId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.getApplicationsForJob(jobId, employerId);
    }

    @GetMapping("/applications/{applicationId}")
    public JobApplicationRequest getApplicationDetails(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.getApplicationDetails(applicationId, employerId);
    }

    @PutMapping("/applications/{applicationId}/status")
    public JobApplicationRequest updateStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.updateApplicationStatus(applicationId, status, employerId);
    }

    // Convenience actions for the UI buttons
    @PostMapping("/applications/{applicationId}/interview")
    public JobApplicationRequest acceptForInterview(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.updateApplicationStatus(applicationId, ApplicationStatus.INTERVIEW, employerId);
    }

    @PostMapping("/applications/{applicationId}/offer")
    public JobApplicationRequest offerJob(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.updateApplicationStatus(applicationId, ApplicationStatus.OFFERED, employerId);
    }

    @PostMapping("/applications/{applicationId}/reject")
    public JobApplicationRequest rejectCandidate(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.updateApplicationStatus(applicationId, ApplicationStatus.REJECTED, employerId);
    }

    @PostMapping("/applications/{applicationId}/request")
    public JobApplicationRequest sendRequestToCandidate(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.updateApplicationStatus(applicationId, ApplicationStatus.REQUEST_SENT, employerId);
    }

    @PostMapping("/applications/{applicationId}/match")
    public JobApplicationRequest markMatchingCandidate(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        Long employerId = resolveEmployerId(email, auth);
        return jobService.updateApplicationStatus(applicationId, ApplicationStatus.MATCHING, employerId);
    }

    private Long resolveEmployerId(String email, Authentication auth) {
        CustomUserDetails principal = auth != null ? (CustomUserDetails) auth.getPrincipal() : null;
        String authRole = principal != null ? principal.getRole() : null;

        // Mirror the "organization info" pattern: use email identity.
        // If email isn't provided, fall back to token subject (username).
        if (email == null || email.isBlank()) {
            email = principal != null ? principal.getUsername() : null;
        }
        if (email == null || email.isBlank()) {
            // In practice this means: UI didn't pass ?email= and token doesn't contain a usable subject/email.
            throw new com.et.SudburyCityPlatform.exception.ForbiddenException("Employer identity missing in token");
        }

        Employer employer = employerRepository.findByEmail(email)
                .orElseThrow(() -> new com.et.SudburyCityPlatform.exception.ResourceNotFoundException("Employer not found"));
        Long requestedEmployerId = employer.getId();

        // EMPLOYER cannot request someone else's email; ADMIN can
        if ("ADMIN".equals(authRole)) {
            return requestedEmployerId;
        }

        // For EMPLOYER, enforce that requested email matches token subject
        String authEmail = principal != null ? principal.getUsername() : null;
        if (authEmail == null || !authEmail.equalsIgnoreCase(email)) {
            throw new com.et.SudburyCityPlatform.exception.ForbiddenException("Unauthorized access");
        }
        return requestedEmployerId;
    }
}

