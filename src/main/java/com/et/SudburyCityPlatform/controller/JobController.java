package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.dto.ApplyJobRequestDTO;
import com.et.SudburyCityPlatform.dto.JobMatchDTO;
import com.et.SudburyCityPlatform.dto.JobSeekerInviteDTO;
import com.et.SudburyCityPlatform.dto.JobSeekerNotificationDTO;
import com.et.SudburyCityPlatform.exception.BadRequestException;
import com.et.SudburyCityPlatform.models.jobs.*;
import com.et.SudburyCityPlatform.service.Jobs.JobService;
import com.et.SudburyCityPlatform.service.Jobs.SavedJobService;
import com.et.SudburyCityPlatform.service.ResumeParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(
        name = "Job Management",
        description = "APIs for creating, updating, and retrieving job openings"
)
public class JobController {

    private final JobService jobService;

    private final ResumeParserService resumeParserService;
    private final SavedJobService savedJobService;

    public JobController(JobService jobService, ResumeParserService resumeParserService, SavedJobService savedJobService) {
        this.jobService = jobService;
        this.resumeParserService = resumeParserService;
        this.savedJobService = savedJobService;
    }

    @PostMapping("/job")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Job> addJob(
            @RequestBody Job job,
            Authentication auth) {

        Long employerId =
                ((CustomUserDetails) auth.getPrincipal()).getEmployerId();

        job.setEmployer(new Employer(employerId));
        Job createdJob = jobService.createJob(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
    }

    @GetMapping("/job")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Get All Jobs",
            description = "Fetches all available job openings"
    )
    public List<Job> getAllJobs(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return jobService.getAllJobsWithMatchPercentage(email);
        }
        return jobService.getAllJobs();
    }

    @PutMapping("/job")
    @Operation(
            summary = "Update Job",
            description = "Updates an existing job opening using job ID"
    )
    public ResponseEntity<Job> updateJob(@RequestBody Job job) throws Throwable {
        Job updatedJob = jobService.updateJob(job);
        return new ResponseEntity<>(updatedJob, HttpStatus.OK);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file) throws Exception {

        return ResponseEntity.ok(resumeParserService.parseResume(file));
    }
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<JobApplicationRequest> applyForJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobApplicationRequest request) {

        JobApplicationRequest saved = jobService.applyForJob(jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Apply for a job using ApplyJobRequestDTO (lightweight model; no JobSeekerProfile / other details).
     * All string fields are limited to 255 chars to avoid DB varchar(255) errors.
     */
    @PostMapping("/{jobId}/apply-with-profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<JobApplicationRequest> applyForJobWithProfile(
            @PathVariable Long jobId,
            @Valid @RequestBody ApplyJobRequestDTO request) {

        JobApplicationRequest saved = jobService.applyForJobWithProfile(jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    /**
     * List of all jobs the job seeker has applied to.
     * Use ?email= for unauthenticated call when /api/** is permitted.
     */
    @GetMapping("/jobseeker/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public List<JobApplicationRequest> getMyApplications(
            @RequestParam(required = false) String email,
            Authentication auth) {
        String seekerEmail = email != null && !email.isBlank() ? email : (auth != null ? auth.getName() : null);
        if (seekerEmail == null || seekerEmail.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return jobService.getApplicationsByEmail(seekerEmail);
    }

    /**
     * Applications where the student has been approved (HIRED, OFFERED) or rejected (REJECTED).
     * Use ?email= for unauthenticated call when /api/** is permitted.
     */
    @GetMapping("/jobseeker/applications/decided")
    @PreAuthorize("hasRole('STUDENT')")
    public List<JobApplicationRequest> getMyDecidedApplications(
            @RequestParam(required = false) String email,
            Authentication auth) {
        String seekerEmail = email != null && !email.isBlank() ? email : (auth != null ? auth.getName() : null);
        if (seekerEmail == null || seekerEmail.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return jobService.getDecidedApplicationsByEmail(seekerEmail);
    }

    /**
     * Recruiter-action notifications for the job seeker (request sent, interview, offered, hired, matching).
     * Each notification includes jobId so the UI can show which job on click.
     */
    @GetMapping("/jobseeker/notifications")
    @PreAuthorize("hasRole('STUDENT')")
    public List<JobSeekerNotificationDTO> getNotifications(
            @RequestParam(required = false) String email,
            Authentication auth) {
        String seekerEmail = email != null && !email.isBlank() ? email : (auth != null ? auth.getName() : null);
        if (seekerEmail == null || seekerEmail.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return jobService.getRecruiterActionNotifications(seekerEmail);
    }

    /**
     * Invites sent to this job seeker (employer invited them to apply). Show in profile/notifications.
     * Each item includes jobId so the UI can show which job on click.
     */
    @GetMapping("/jobseeker/invites")
    @PreAuthorize("hasRole('STUDENT')")
    public List<JobSeekerInviteDTO> getInvites(
            @RequestParam(required = false) String email,
            Authentication auth) {
        String seekerEmail = email != null && !email.isBlank() ? email : (auth != null ? auth.getName() : null);
        if (seekerEmail == null || seekerEmail.isBlank()) {
            throw new BadRequestException("Email is required");
        }
        return jobService.getInvitesForJobSeeker(seekerEmail);
    }

    @GetMapping("/search")
    public List<Job> search(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minSalary) {
        return jobService.searchJobs(location, type, minSalary);
    }

    @PostMapping("/jobs/{jobId}/save")
    public void saveJob(
            @PathVariable Long jobId,
            @RequestParam String email) {
        savedJobService.save(email, jobId);
    }

    @GetMapping("/jobs/saved")
    public List<Job> saved(@RequestParam String email) {
        return savedJobService.getSaved(email);
    }
    @GetMapping("/jobs/recommended")
    public List<Job> recommended(@RequestParam String email) {
        return jobService.matchJobsByProfile(email);
    }

    /**
     * Jobs list with match percentage for a particular job seeker.
     * Returns top 20 recent jobs scored by skills + experience.
     */
    @GetMapping("/jobs/matches")
    @PreAuthorize("hasAnyRole('STUDENT','EMPLOYER','ADMIN')")
    public List<JobMatchDTO> jobMatches(@RequestParam String email) {
        return jobService.getJobMatchesForJobSeeker(email);
    }

}
