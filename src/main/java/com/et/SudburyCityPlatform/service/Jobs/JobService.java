package com.et.SudburyCityPlatform.service.Jobs;

import com.et.SudburyCityPlatform.dto.ApplyJobRequestDTO;
import com.et.SudburyCityPlatform.dto.EducationDTO;
import com.et.SudburyCityPlatform.dto.EmployerAcceptedCandidateDTO;
import com.et.SudburyCityPlatform.dto.EmployerDashboardMetricsDTO;
import com.et.SudburyCityPlatform.dto.JobMatchDTO;
import com.et.SudburyCityPlatform.dto.JobSeekerInviteDTO;
import com.et.SudburyCityPlatform.dto.JobSeekerNotificationDTO;
import com.et.SudburyCityPlatform.dto.EmployerJobStatsDTO;
import com.et.SudburyCityPlatform.dto.EmployerJobPostRequestDTO;
import com.et.SudburyCityPlatform.exception.AlreadyAppliedException;
import com.et.SudburyCityPlatform.exception.BadRequestException;
import com.et.SudburyCityPlatform.exception.ForbiddenException;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.jobs.*;
import com.et.SudburyCityPlatform.repository.Jobs.JobApplicationRepository;
import com.et.SudburyCityPlatform.repository.Jobs.JobInviteRepository;
import com.et.SudburyCityPlatform.repository.Jobs.JobRepository;
import com.et.SudburyCityPlatform.repository.Jobs.JobSeekerProfileRepository;
import com.et.SudburyCityPlatform.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

@Service
public class JobService {

    private final JobRepository jobRepository;

   private final JobApplicationRepository applicationRepository;

    @Autowired
    private JobSeekerProfileRepository profileRepository;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired
    private JobInviteRepository jobInviteRepository;

    @Autowired
    public JobService(JobRepository jobRepository, JobApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;


    }

    public Job createJob(Job job) {
        job.setPostedDate(LocalDate.now());
        return jobRepository.save(job);
    }

    /**
     * Employer-scoped job creation for the "Post a Job" form.
     */
    public Job createJobForEmployer(Long employerId, EmployerJobPostRequestDTO dto) {
        Job job = new Job();
        job.setEmployer(new Employer(employerId));
        applyEmployerDto(job, dto, true);
        return jobRepository.save(job);
    }

    /**
     * Employer-scoped job update. Only the owner employer can update.
     */
    public Job updateJobForEmployer(Long employerId, Long jobId, EmployerJobPostRequestDTO dto) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (job.getEmployer() == null || job.getEmployer().getId() == null || !job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized access");
        }
        applyEmployerDto(job, dto, false);
        return jobRepository.save(job);
    }

    public Job getJobForEmployer(Long employerId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (job.getEmployer() == null || job.getEmployer().getId() == null || !job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized access");
        }
        return job;
    }

    public void deleteJobForEmployer(Long employerId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (job.getEmployer() == null || job.getEmployer().getId() == null || !job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized access");
        }
        jobRepository.delete(job);
    }

    /**
     * Employer invites a job seeker (by email) to apply for a job. Creates a JobInvite record and sends an invite email.
     */
    public JobInvite inviteToApply(Long jobId, String inviteeEmail, Long employerId) {
        if (inviteeEmail == null || inviteeEmail.isBlank()) {
            throw new BadRequestException("Invitee email is required");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (job.getEmployer() == null || job.getEmployer().getId() == null || !job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized access");
        }

        JobInvite invite = new JobInvite();
        invite.setJob(job);
        invite.setInviteeEmail(inviteeEmail.trim());
        invite = jobInviteRepository.save(invite);

        if (emailService != null) {
            emailService.sendInviteToApplyEmail(job, invite.getInviteeEmail());
        }

        return invite;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    /**
     * Get all jobs but enrich each job with a computed `matchPercentage` for the given job seeker.
     * Response is sorted by highest match first.
     */
    public List<Job> getAllJobsWithMatchPercentage(String email) {
        JobSeekerProfile profile =
                profileRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Profile required"));

        List<Job> jobs = jobRepository.findAll();
        for (Job j : jobs) {
            int pct = scoreJob(profile, j).getMatchPercentage();
            j.setMatchPercentage(pct);
        }
        jobs.sort((a, b) -> {
            int ap = a.getMatchPercentage() != null ? a.getMatchPercentage() : 0;
            int bp = b.getMatchPercentage() != null ? b.getMatchPercentage() : 0;
            int cmp = Integer.compare(bp, ap);
            if (cmp != 0) return cmp;
            // tie-breaker: latest postedDate first (nulls last)
            if (a.getPostedDate() == null && b.getPostedDate() == null) return 0;
            if (a.getPostedDate() == null) return 1;
            if (b.getPostedDate() == null) return -1;
            return b.getPostedDate().compareTo(a.getPostedDate());
        });
        return jobs;
    }

    public Job updateJob(Job job) {
        Job desiredJob = jobRepository.findById(job.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Job not found with id: " + job.getId())
                );

        desiredJob.setDescription(job.getDescription());
        desiredJob.setSalary(job.getSalary());
        desiredJob.setSalaryMin(job.getSalaryMin());
        desiredJob.setSalaryMax(job.getSalaryMax());
        desiredJob.setEmploymentType(job.getEmploymentType());
        desiredJob.setPostedDate(LocalDate.now());
        desiredJob.setRequirements(job.getRequirements());
        desiredJob.setRole(job.getRole());
        desiredJob.setLocation(job.getLocation());
        desiredJob.setTypeOfWork(job.getTypeOfWork());
        desiredJob.setAddress(job.getAddress());
        desiredJob.setExperienceRange(job.getExperienceRange());
        desiredJob.setPreferredLanguage(job.getPreferredLanguage());
        desiredJob.setUrgentlyHiring(job.getUrgentlyHiring());
        desiredJob.setCompanyName(job.getCompanyName());

        return jobRepository.save(desiredJob);
    }



    public JobApplicationRequest applyForJob(Long jobId, JobApplicationRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        boolean alreadyApplied =
                applicationRepository.existsByJobIdAndEmail(jobId, request.getEmail());

        if (alreadyApplied) {
            throw new AlreadyAppliedException("You have already applied for this job");
        }

        JobSeekerProfile profile =
                profileRepository.findByEmail(request.getEmail()).orElse(null);

        if (profile != null) {
            request.setResumeUrl(profile.getResumeUrl());
            request.setYearsOfExperience(profile.getYearsOfExperience());

            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                request.setFirstName(profile.getFullName());
            }
        }
        request.setJob(job);
        return applicationRepository.save(request);
    }

    /**
     * Apply for a job using ApplyJobRequestDTO (lightweight model, all strings safe for varchar(255)).
     */
    public JobApplicationRequest applyForJobWithProfile(Long jobId, ApplyJobRequestDTO dto) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        String email = dto.getBasicInfo() != null && dto.getBasicInfo().getEmail() != null
                ? dto.getBasicInfo().getEmail().trim() : null;
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required to apply (in basicInfo)");
        }

        if (applicationRepository.existsByJobIdAndEmail(jobId, email)) {
            throw new AlreadyAppliedException("You have already applied for this job");
        }

        JobApplicationRequest request = mapApplyDtoToApplication(dto, job);
        return applicationRepository.save(request);
    }

    private static final int MAX_LEN = 255;

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    private JobApplicationRequest mapApplyDtoToApplication(ApplyJobRequestDTO dto, Job job) {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setJob(job);

        String fullName = dto.getBasicInfo() != null && dto.getBasicInfo().getName() != null
                ? dto.getBasicInfo().getName().trim() : "";
        if (fullName.isBlank()) fullName = "Applicant";
        int firstSpace = fullName.indexOf(' ');
        String firstName = firstSpace > 0 ? fullName.substring(0, firstSpace) : fullName;
        String lastName = firstSpace > 0 ? fullName.substring(firstSpace + 1).trim() : "";

        request.setFirstName(truncate(firstName, MAX_LEN));
        request.setLastName(truncate(lastName.isEmpty() ? "" : lastName, MAX_LEN));
        request.setEmail(truncate(dto.getBasicInfo() != null ? dto.getBasicInfo().getEmail() : null, MAX_LEN));
        String phone = dto.getBasicInfo() != null && dto.getBasicInfo().getPhone() != null && !dto.getBasicInfo().getPhone().isBlank()
                ? dto.getBasicInfo().getPhone() : "N/A";
        request.setPhoneNumber(truncate(phone, MAX_LEN));
        request.setCity(truncate(dto.getCity(), MAX_LEN));
        request.setZipCode(truncate(dto.getPostalCode(), MAX_LEN));
        request.setPositionAppliedFor(truncate(job.getRole() != null ? job.getRole() : "Position", MAX_LEN));
        request.setYearsOfExperience(dto.getYearsOfExperience());
        request.setResumeUrl(truncate(dto.getResumeUrl(), MAX_LEN));
        String linkedin = dto.getBasicInfo() != null ? dto.getBasicInfo().getLinkedin() : null;
        request.setLinkedInProfile(truncate(linkedin, MAX_LEN));
        request.setCoverLetter(null); // no summary in ApplyJobRequestDTO
        String highestEducation = null;
        if (dto.getEducation() != null && !dto.getEducation().isEmpty()) {
            EducationDTO first = dto.getEducation().get(0);
            highestEducation = first.getDegree();
            if (first.getFieldOfStudy() != null && !first.getFieldOfStudy().isBlank()) {
                highestEducation = (highestEducation != null ? highestEducation : "") + " in " + first.getFieldOfStudy();
            }
        }
        request.setHighestEducation(truncate(highestEducation, MAX_LEN));
        request.setAuthorizedToWork(true);
        request.setRequireSponsorship(false);
        request.setStatus(ApplicationStatus.APPLIED);

        return request;
    }

    private void applyEmployerDto(Job job, EmployerJobPostRequestDTO dto, boolean isCreate) {
        job.setRole(dto.getRole());
        job.setCompanyName(dto.getCompanyName());
        job.setLocation(dto.getJobLocation());
        job.setAddress(dto.getAddress());
        job.setExperienceRange(dto.getExperienceRange());
        job.setEmploymentType(dto.getEmploymentType());
        job.setTypeOfWork(dto.getTypeOfWork());
        job.setPreferredLanguage(dto.getPreferredLanguage());
        job.setUrgentlyHiring(dto.getUrgentlyHiring());
        job.setDescription(dto.getJobDescription());
        job.setRequirements(dto.getRequirements());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setExternalApplyUrl(dto.getExternalApplyUrl() != null && !dto.getExternalApplyUrl().isBlank() ? dto.getExternalApplyUrl().trim() : null);

        // Keep legacy salary field populated for existing code paths
        if (dto.getSalaryMin() != null && dto.getSalaryMax() != null) {
            job.setSalary((dto.getSalaryMin() + dto.getSalaryMax()) / 2.0);
        } else if (dto.getSalaryMax() != null) {
            job.setSalary(dto.getSalaryMax());
        } else if (dto.getSalaryMin() != null) {
            job.setSalary(dto.getSalaryMin());
        }

        if (isCreate) {
            job.setPostedDate(LocalDate.now());
        }
    }


    public List<Job> getJobsByEmployer(Long employerId) {
        return jobRepository.findByEmployerId(employerId);
    }

    /**
     * Returns all jobs posted by an employer along with candidate counts.
     * Counts are derived from JobApplicationRequest.status:
     * - appliedCandidates: total applications
     * - acceptedCandidates: OFFERED + HIRED
     * - declinedCandidates: REJECTED
     * - requestsSent: REQUEST_SENT
     * - matchingCandidates: MATCHING
     */
    public List<EmployerJobStatsDTO> getEmployerJobsWithStats(Long employerId) {
        List<Job> jobs = getJobsByEmployer(employerId);
        if (jobs.isEmpty()) return List.of();

        List<Long> jobIds = jobs.stream().map(Job::getId).toList();
        List<JobApplicationRequest> apps = applicationRepository.findByJobIdIn(jobIds);

        Map<Long, List<JobApplicationRequest>> byJob = new HashMap<>();
        for (JobApplicationRequest a : apps) {
            Long jid = a.getJob() != null ? a.getJob().getId() : null;
            if (jid == null) continue;
            byJob.computeIfAbsent(jid, k -> new ArrayList<>()).add(a);
        }

        List<EmployerJobStatsDTO> out = new ArrayList<>();
        for (Job j : jobs) {
            List<JobApplicationRequest> list = byJob.getOrDefault(j.getId(), List.of());
            long total = list.size();
            long accepted = list.stream().filter(a -> a.getStatus() == ApplicationStatus.HIRED || a.getStatus() == ApplicationStatus.OFFERED).count();
            long declined = list.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count();
            long requestsSent = list.stream().filter(a -> a.getStatus() == ApplicationStatus.REQUEST_SENT).count();
            long matching = list.stream().filter(a -> a.getStatus() == ApplicationStatus.MATCHING).count();
            out.add(new EmployerJobStatsDTO(
                    j.getId(),
                    j.getRole(),
                    j.getCompanyName(),
                    total,
                    accepted,
                    declined,
                    requestsSent,
                    matching
            ));
        }
        return out;
    }

    /**
     * Employer dashboard: list all candidates that were accepted for this employer's jobs.
     * Accepted = OFFERED or HIRED.
     *
     * Includes matchPercentage for each candidate against the specific job.
     */
    public List<EmployerAcceptedCandidateDTO> getAcceptedCandidatesForEmployer(Long employerId) {
        List<Job> jobs = getJobsByEmployer(employerId);
        if (jobs.isEmpty()) return List.of();

        List<Long> jobIds = jobs.stream().map(Job::getId).toList();
        List<JobApplicationRequest> apps = applicationRepository.findByJobIdIn(jobIds);

        List<EmployerAcceptedCandidateDTO> out = new ArrayList<>();
        for (JobApplicationRequest app : apps) {
            if (app.getStatus() != ApplicationStatus.OFFERED && app.getStatus() != ApplicationStatus.HIRED) {
                continue;
            }
            Job job = app.getJob();
            if (job == null) continue;

            String name = (app.getFirstName() != null ? app.getFirstName().trim() : "") +
                    (app.getLastName() != null && !app.getLastName().isBlank() ? " " + app.getLastName().trim() : "");
            if (name.isBlank()) name = app.getEmail();

            Integer matchPct = 0;
            JobSeekerProfile seekerProfile = null;
            if (app.getEmail() != null && !app.getEmail().isBlank()) {
                seekerProfile = profileRepository.findByEmail(app.getEmail()).orElse(null);
            }
            if (seekerProfile != null) {
                matchPct = scoreJob(seekerProfile, job).getMatchPercentage();
            } else {
                // fallback: experience-only match if profile doesn't exist
                matchPct = experienceMatchPercentage(app.getYearsOfExperience(), job.getExperienceRange());
            }

            out.add(new EmployerAcceptedCandidateDTO(
                    app.getId(),
                    job.getId(),
                    app.getEmail(),
                    name.trim(),
                    app.getYearsOfExperience(),
                    job.getRole(),
                    job.getLocation(),
                    matchPct,
                    app.getStatus()
            ));
        }

        out.sort((a, b) -> {
            int ap = a.getMatchPercentage() != null ? a.getMatchPercentage() : 0;
            int bp = b.getMatchPercentage() != null ? b.getMatchPercentage() : 0;
            return Integer.compare(bp, ap);
        });
        return out;
    }

    /**
     * Employer dashboard metrics (cards + acceptance-rate chart + attention needed).
     * This is intentionally lightweight and uses simple heuristics so the UI can render.
     */
    public EmployerDashboardMetricsDTO getEmployerDashboardMetrics(Long employerId, Integer windowDays) {
        int window = (windowDays == null || windowDays <= 0) ? 365 : windowDays;
        if (window != 7 && window != 30 && window != 90 && window != 365) {
            // keep it flexible but predictable for UI toggles
            window = windowDays;
        }

        List<Job> jobs = getJobsByEmployer(employerId);
        long activeJobs = jobs.size();

        List<JobApplicationRequest> apps;
        if (jobs.isEmpty()) {
            apps = List.of();
        } else {
            List<Long> jobIds = jobs.stream().map(Job::getId).toList();
            apps = applicationRepository.findByJobIdIn(jobIds);
        }

        long matchingApplicants = apps.stream().filter(a -> a.getStatus() == ApplicationStatus.MATCHING).count();
        double avgApplicantsPerJob = activeJobs > 0 ? (matchingApplicants * 1.0 / activeJobs) : 0.0;

        long accepted = apps.stream().filter(a -> a.getStatus() == ApplicationStatus.OFFERED || a.getStatus() == ApplicationStatus.HIRED).count();

        // % change vs previous window using appliedAt as the time dimension
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(window);
        LocalDateTime prevStart = now.minusDays(window * 2L);

        long acceptedThisWindow = apps.stream()
                .filter(a -> a.getAppliedAt() != null && !a.getAppliedAt().isBefore(start))
                .filter(a -> a.getStatus() == ApplicationStatus.OFFERED || a.getStatus() == ApplicationStatus.HIRED)
                .count();
        long acceptedPrevWindow = apps.stream()
                .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().isBefore(start) && !a.getAppliedAt().isBefore(prevStart))
                .filter(a -> a.getStatus() == ApplicationStatus.OFFERED || a.getStatus() == ApplicationStatus.HIRED)
                .count();
        Integer acceptedChangePct = percentChange(acceptedPrevWindow, acceptedThisWindow);

        // Active jobs change: compare jobs posted in last 30 days vs previous 30 days (best effort)
        LocalDate today = LocalDate.now();
        long jobsThisMonth = jobs.stream().filter(j -> j.getPostedDate() != null && !j.getPostedDate().isBefore(today.minusDays(30))).count();
        long jobsPrevMonth = jobs.stream().filter(j -> j.getPostedDate() != null && j.getPostedDate().isBefore(today.minusDays(30)) && !j.getPostedDate().isBefore(today.minusDays(60))).count();
        Integer activeJobsChangePct = percentChange(jobsPrevMonth, jobsThisMonth);

        // Acceptance rate series: per-day accepted/(accepted+rejected) for applications applied on that day
        List<EmployerDashboardMetricsDTO.AcceptanceRatePoint> series = new ArrayList<>();
        List<EmployerDashboardMetricsDTO.AcceptanceRatePoint> projected = new ArrayList<>();

        LocalDate startDate = today.minusDays(Math.max(1, window - 1));
        for (int i = 0; i < window; i++) {
            LocalDate d = startDate.plusDays(i);
            long decided = apps.stream()
                    .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().toLocalDate().equals(d))
                    .filter(a -> a.getStatus() == ApplicationStatus.OFFERED || a.getStatus() == ApplicationStatus.HIRED || a.getStatus() == ApplicationStatus.REJECTED)
                    .count();
            long acceptedDecided = apps.stream()
                    .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().toLocalDate().equals(d))
                    .filter(a -> a.getStatus() == ApplicationStatus.OFFERED || a.getStatus() == ApplicationStatus.HIRED)
                    .count();

            int rate = decided == 0 ? 0 : (int) Math.round((acceptedDecided * 100.0) / decided);
            series.add(new EmployerDashboardMetricsDTO.AcceptanceRatePoint(d, clampPct(rate)));
        }

        // Projected: simple 7-day moving average of actuals (dashed line in UI)
        int ma = 7;
        for (int i = 0; i < series.size(); i++) {
            int from = Math.max(0, i - ma + 1);
            int sum = 0;
            int cnt = 0;
            for (int k = from; k <= i; k++) {
                sum += series.get(k).getAcceptanceRatePct();
                cnt++;
            }
            int avg = cnt == 0 ? 0 : (int) Math.round(sum * 1.0 / cnt);
            projected.add(new EmployerDashboardMetricsDTO.AcceptanceRatePoint(series.get(i).getDate(), clampPct(avg)));
        }

        // Attention needed
        long pendingReview = apps.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_REVIEW).count();
        long jobsNearingDeadline = jobs.stream()
                .filter(j -> j.getPostedDate() != null)
                .filter(j -> j.getPostedDate().isBefore(today.minusDays(25)) && j.getPostedDate().isAfter(today.minusDays(40)))
                .count();

        // low match jobs heuristic: average match% across all seekers who applied is below 40
        long lowMatchJobs = 0;
        for (Job job : jobs) {
            List<JobApplicationRequest> jobApps = apps.stream().filter(a -> a.getJob() != null && job.getId() != null && job.getId().equals(a.getJob().getId())).toList();
            if (jobApps.isEmpty()) continue;
            int scored = 0;
            int sum = 0;
            for (JobApplicationRequest app : jobApps) {
                if (app.getEmail() == null || app.getEmail().isBlank()) continue;
                JobSeekerProfile p = profileRepository.findByEmail(app.getEmail()).orElse(null);
                if (p == null) continue;
                sum += scoreJob(p, job).getMatchPercentage();
                scored++;
            }
            if (scored == 0) continue;
            int avg = (int) Math.round(sum * 1.0 / scored);
            if (avg < 40) lowMatchJobs++;
        }

        List<String> notes = new ArrayList<>();
        if (pendingReview > 0) notes.add(pendingReview + " candidates pending review");
        if (jobsNearingDeadline > 0) notes.add(jobsNearingDeadline + " job nearing deadline");
        if (lowMatchJobs > 0) notes.add("Low match rate for " + lowMatchJobs + " roles");
        if (notes.isEmpty()) notes.add("No issues detected");

        EmployerDashboardMetricsDTO.AttentionNeeded attention =
                new EmployerDashboardMetricsDTO.AttentionNeeded(
                        pendingReview,
                        jobsNearingDeadline,
                        lowMatchJobs,
                        notes
                );

        return new EmployerDashboardMetricsDTO(
                window,
                matchingApplicants,
                activeJobs,
                round2(avgApplicantsPerJob),
                accepted,
                acceptedChangePct,
                activeJobsChangePct,
                series,
                projected,
                attention
        );
    }

    private static Integer percentChange(long prev, long curr) {
        if (prev <= 0) return null;
        double pct = ((curr - prev) * 100.0) / prev;
        return (int) Math.round(pct);
    }

    private static int clampPct(int v) {
        return Math.max(0, Math.min(100, v));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public List<JobApplicationRequest> getApplicationsForJob(Long jobId, Long employerId) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized access");
        }

        List<JobApplicationRequest> apps = applicationRepository.findByJobId(jobId);
        if (apps.isEmpty()) return apps;

        // Batch-load profiles to avoid N+1 queries
        List<String> emails = apps.stream()
                .map(JobApplicationRequest::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .map(e -> e.trim().toLowerCase())
                .distinct()
                .toList();

        Map<String, JobSeekerProfile> byEmail = emails.isEmpty()
                ? Map.of()
                : profileRepository.findByEmailIn(emails).stream()
                .filter(p -> p.getEmail() != null && !p.getEmail().isBlank())
                .collect(java.util.stream.Collectors.toMap(
                        p -> p.getEmail().trim().toLowerCase(),
                        Function.identity(),
                        (a, b) -> a
                ));

        for (JobApplicationRequest app : apps) {
            Integer matchPct;
            String email = app.getEmail() != null ? app.getEmail().trim().toLowerCase() : null;
            JobSeekerProfile seekerProfile = (email != null && !email.isBlank()) ? byEmail.get(email) : null;
            if (seekerProfile != null) {
                matchPct = scoreJob(seekerProfile, job).getMatchPercentage();
            } else {
                // fallback: experience-only match if profile doesn't exist
                matchPct = experienceMatchPercentage(app.getYearsOfExperience(), job.getExperienceRange());
            }
            app.setMatchPercentage(matchPct);
        }

        return apps;
    }

    public List<JobApplicationRequest> getApplicationsByEmail(String email) {
        return applicationRepository.findByEmail(email);
    }

    /**
     * Applications where the job seeker was approved (HIRED, OFFERED) or rejected (REJECTED).
     */
    public List<JobApplicationRequest> getDecidedApplicationsByEmail(String email) {
        return applicationRepository.findByEmailAndStatusIn(
                email,
                List.of(ApplicationStatus.HIRED, ApplicationStatus.OFFERED, ApplicationStatus.REJECTED)
        );
    }

    /**
     * Notifications for a job seeker: recruiter actions (request sent, interview, offered, hired, matching).
     * Each item includes jobId so the UI can show "which job" on click.
     */
    public List<JobSeekerNotificationDTO> getRecruiterActionNotifications(String email) {
        List<JobApplicationRequest> applications = applicationRepository.findByEmailAndStatusIn(
                email,
                List.of(
                        ApplicationStatus.REQUEST_SENT,
                        ApplicationStatus.MATCHING,
                        ApplicationStatus.INTERVIEW,
                        ApplicationStatus.OFFERED,
                        ApplicationStatus.HIRED
                )
        );

        List<JobSeekerNotificationDTO> out = new ArrayList<>();
        for (JobApplicationRequest app : applications) {
            Job job = app.getJob();
            if (job == null) continue;

            String companyName = (job.getEmployer() != null && job.getEmployer().getCompanyName() != null && !job.getEmployer().getCompanyName().isBlank())
                    ? job.getEmployer().getCompanyName()
                    : (job.getCompanyName() != null && !job.getCompanyName().isBlank() ? job.getCompanyName() : "Employer");
            String jobRole = job.getRole() != null && !job.getRole().isBlank() ? job.getRole() : "the position you applied for";

            String message = messageForStatus(app.getStatus(), companyName, jobRole);

            out.add(new JobSeekerNotificationDTO(
                    app.getId(),
                    job.getId(),
                    jobRole,
                    companyName,
                    app.getStatus(),
                    message,
                    app.getAppliedAt() != null ? app.getAppliedAt() : java.time.LocalDateTime.now()
            ));
        }

        out.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        return out;
    }

    /**
     * Invites sent to this job seeker (by email). Employer invited them to apply for a job.
     * Use jobId on click to show which job.
     */
    public List<JobSeekerInviteDTO> getInvitesForJobSeeker(String email) {
        if (email == null || email.isBlank()) return List.of();
        List<JobInvite> invites = jobInviteRepository.findByInviteeEmailOrderByInvitedAtDesc(email.trim());
        List<JobSeekerInviteDTO> out = new ArrayList<>();
        for (JobInvite inv : invites) {
            Job job = inv.getJob();
            if (job == null) continue;
            String companyName = (job.getEmployer() != null && job.getEmployer().getCompanyName() != null && !job.getEmployer().getCompanyName().isBlank())
                    ? job.getEmployer().getCompanyName()
                    : (job.getCompanyName() != null && !job.getCompanyName().isBlank() ? job.getCompanyName() : "Employer");
            String jobRole = job.getRole() != null && !job.getRole().isBlank() ? job.getRole() : "a matching job";
            String message = "Recruiter from " + companyName + " sent an invitation request for a matching job: " + jobRole + ".";
            out.add(new JobSeekerInviteDTO(
                    inv.getId(),
                    job.getId(),
                    jobRole,
                    companyName,
                    message,
                    inv.getInvitedAt()
            ));
        }
        return out;
    }

    private static String messageForStatus(ApplicationStatus status, String companyName, String jobRole) {
        return switch (status) {
            case REQUEST_SENT -> "Recruiter from " + companyName + " sent an invitation request for a matching job: " + jobRole + ".";
            case MATCHING -> "Recruiter from " + companyName + " marked you as a matching candidate for " + jobRole + ".";
            case INTERVIEW -> "Recruiter from " + companyName + " accepted you for the interview process for " + jobRole + ".";
            case OFFERED -> "Recruiter from " + companyName + " offered you the job: " + jobRole + ".";
            case HIRED -> "You have been hired by " + companyName + " for " + jobRole + ".";
            default -> "Update from " + companyName + " for " + jobRole + ".";
        };
    }

    public JobApplicationRequest updateApplicationStatus(
            Long applicationId,
            ApplicationStatus status,
            Long employerId) {

        JobApplicationRequest application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Application not found"));

        Job job = application.getJob();

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized status update");
        }

        application.setStatus(status);

        JobApplicationRequest saved = applicationRepository.save(application);

        // Send status email (non-blocking; EmailService no-ops if mail not configured)
        if (emailService != null) {
            emailService.sendApplicationStatusEmail(saved, status);
        }

        return saved;
    }

    public JobApplicationRequest getApplicationDetails(Long applicationId, Long employerId) {

        JobApplicationRequest application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Application not found"));

        Job job = application.getJob();

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized access to application");
        }

        return application;
    }
    public List<Job> searchJobs(String location, String type, Double minSalary) {
        return jobRepository.search(location, type, minSalary);
    }
    public List<Job> matchJobsByProfile(String email) {

        JobSeekerProfile profile =
                profileRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Profile required"));

        return jobRepository.findAll().stream()
                .filter(job ->
                        profile.getSkills().stream().anyMatch(skill ->
                                job.getRequirements() != null &&
                                        job.getRequirements()
                                                .toLowerCase()
                                                .contains(skill.toLowerCase())
                        )
                )
                .toList();
    }

    /**
     * Returns jobs with match percentage for a given job seeker.
     * Scoring is heuristic:
     * - 70% skills match (profile skills present in job text: requirements/description/role)
     * - 30% experience match (yearsOfExperience vs job.experienceRange)
     */
    public List<JobMatchDTO> getJobMatchesForJobSeeker(String email) {
        JobSeekerProfile profile =
                profileRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("Profile required"));

        List<Job> jobs = jobRepository.findTop20ByOrderByPostedDateDesc();
        List<JobMatchDTO> out = new ArrayList<>();
        for (Job job : jobs) {
            out.add(scoreJob(profile, job));
        }
        out.sort((a, b) -> Integer.compare(b.getMatchPercentage(), a.getMatchPercentage()));
        return out;
    }

    private static final Pattern NON_WORD = Pattern.compile("[^a-z0-9+.#]+");

    private JobMatchDTO scoreJob(JobSeekerProfile profile, Job job) {
        Set<String> seekerSkills = new HashSet<>();
        addNormalizedSkills(seekerSkills, profile.getPrimarySkills());
        addNormalizedSkills(seekerSkills, profile.getSkills());
        addNormalizedSkills(seekerSkills, profile.getBasicSkills());

        String jobText = normalizeText(
                (job.getRole() == null ? "" : job.getRole()) + " " +
                (job.getRequirements() == null ? "" : job.getRequirements()) + " " +
                (job.getDescription() == null ? "" : job.getDescription())
        );

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String skill : seekerSkills) {
            if (skill.isBlank()) continue;
            if (containsSkill(jobText, skill)) {
                matched.add(skill);
            } else {
                missing.add(skill);
            }
        }

        int skillPct;
        if (seekerSkills.isEmpty()) {
            skillPct = 0;
        } else {
            // skills are from the seeker's side; match % = how many of their skills appear in job text
            skillPct = (int) Math.round((matched.size() * 100.0) / seekerSkills.size());
        }

        int expPct = experienceMatchPercentage(profile.getYearsOfExperience(), job.getExperienceRange());

        int finalPct = (int) Math.round(skillPct * 0.70 + expPct * 0.30);
        finalPct = Math.max(0, Math.min(100, finalPct));

        return new JobMatchDTO(
                job,
                finalPct,
                skillPct,
                expPct,
                matched,
                missing
        );
    }

    private static void addNormalizedSkills(Set<String> out, List<String> skills) {
        if (skills == null) return;
        for (String s : skills) {
            String norm = normalizeSkill(s);
            if (norm != null && !norm.isBlank()) out.add(norm);
        }
    }

    private static String normalizeSkill(String s) {
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        if (t.isBlank()) return null;
        // collapse punctuation/spaces but keep common tech symbols (+ . #)
        t = NON_WORD.matcher(t).replaceAll(" ").trim();
        return t;
    }

    private static String normalizeText(String s) {
        if (s == null) return "";
        String t = s.toLowerCase();
        t = NON_WORD.matcher(t).replaceAll(" ");
        return " " + t.trim() + " ";
    }

    private static boolean containsSkill(String normalizedJobText, String normalizedSkill) {
        // Word-boundary-ish check by surrounding spaces after normalization.
        String needle = " " + normalizedSkill + " ";
        if (normalizedJobText.contains(needle)) return true;

        // Also allow simple substring match for skills that include +/#/. (e.g., c++, c#, node.js)
        if (normalizedSkill.contains("+") || normalizedSkill.contains("#") || normalizedSkill.contains(".")) {
            return normalizedJobText.contains(normalizedSkill);
        }
        return false;
    }

    private static int experienceMatchPercentage(Integer yearsOfExperience, String experienceRange) {
        if (experienceRange == null || experienceRange.isBlank()) {
            return 50; // unknown requirement
        }
        if (yearsOfExperience == null) {
            return 0; // seeker didn't provide experience
        }

        String r = experienceRange.trim().toLowerCase();
        // common inputs: "1-2", "2-3", "3-5", "5+"
        try {
            if (r.endsWith("+")) {
                int min = Integer.parseInt(r.substring(0, r.length() - 1).trim());
                return yearsOfExperience >= min ? 100 : (int) Math.round((yearsOfExperience * 100.0) / Math.max(1, min));
            }
            if (r.contains("-")) {
                String[] parts = r.split("-");
                int min = Integer.parseInt(parts[0].trim());
                int max = Integer.parseInt(parts[1].trim());
                if (yearsOfExperience < min) {
                    return (int) Math.round((yearsOfExperience * 100.0) / Math.max(1, min));
                }
                if (yearsOfExperience > max) {
                    return 100;
                }
                return 100;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return 50;
    }

    public List<ApplicationSummary> summary(String email) {
        return applicationRepository.summary(email);
    }
    public EmployerStats stats(Long employerId) {
        long jobs = jobRepository.findByEmployerId(employerId).size();
        long apps = applicationRepository.count();
        long hires = applicationRepository
                .findAll()
                .stream()
                .filter(a -> a.getStatus() == ApplicationStatus.HIRED)
                .count();

        return new EmployerStats(jobs, apps, hires);
    }


}
