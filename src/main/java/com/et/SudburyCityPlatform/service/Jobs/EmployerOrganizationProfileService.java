package com.et.SudburyCityPlatform.service.Jobs;

import com.et.SudburyCityPlatform.dto.EmployerOrganizationProfileRequestDTO;
import com.et.SudburyCityPlatform.exception.ConflictException;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.jobs.Employer;
import com.et.SudburyCityPlatform.models.jobs.EmployerOrganizationProfile;
import com.et.SudburyCityPlatform.repository.Jobs.EmployerRepository;
import com.et.SudburyCityPlatform.repository.Jobs.EmployerOrganizationProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerOrganizationProfileService {

    private final EmployerOrganizationProfileRepository repo;
    private final EmployerRepository employerRepository;
    private final JobService jobService;

    public EmployerOrganizationProfileService(
            EmployerOrganizationProfileRepository repo,
            EmployerRepository employerRepository,
            JobService jobService
    ) {
        this.repo = repo;
        this.employerRepository = employerRepository;
        this.jobService = jobService;
    }

    public EmployerOrganizationProfile create(String email, EmployerOrganizationProfileRequestDTO dto) {
        if (repo.existsByEmail(email)) {
            throw new ConflictException("Employer organization profile already exists");
        }
        ensureEmployerRecord(email, dto);
        EmployerOrganizationProfile p = new EmployerOrganizationProfile();
        p.setEmail(email);
        apply(dto, p);
        return repo.save(p);
    }

    public EmployerOrganizationProfile update(String email, EmployerOrganizationProfileRequestDTO dto) {
        ensureEmployerRecord(email, dto);
        EmployerOrganizationProfile p = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employer organization profile not found"));
        apply(dto, p);
        return repo.save(p);
    }

    public EmployerOrganizationProfile get(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employer organization profile not found"));
    }

    /**
     * Deletes organization profile, all jobs posted by this employer (and related invites/applications/saves),
     * and the {@code employers} row for the same email when present.
     */
    @Transactional
    public void delete(String email) {
        Employer employer = employerRepository.findByEmail(email).orElse(null);
        EmployerOrganizationProfile org = repo.findByEmail(email).orElse(null);
        if (employer == null && org == null) {
            throw new ResourceNotFoundException("Employer organization profile not found");
        }
        if (employer != null) {
            jobService.deleteAllJobsForEmployer(employer.getId());
            employerRepository.delete(employer);
        }
        if (org != null) {
            repo.delete(org);
        }
    }

    private void apply(EmployerOrganizationProfileRequestDTO dto, EmployerOrganizationProfile p) {
        p.setOrganizationName(dto.getOrganizationName());
        p.setAboutOrganization(dto.getAboutOrganization());
        p.setLocation(dto.getLocation());
        p.setFoundedYear(dto.getFoundedYear());
        p.setWebsite(dto.getWebsite());
        p.setCompanySize(dto.getCompanySize());
        p.setIndustry(dto.getIndustry());
    }

    /**
     * Jobs endpoints use the `employers` table (by employerId). Organization profile is stored separately.
     * To avoid "Employer not found" / missing employerId issues, ensure an Employer row exists for this email.
     */
    private void ensureEmployerRecord(String email, EmployerOrganizationProfileRequestDTO dto) {
        employerRepository.findByEmail(email).orElseGet(() -> {
            Employer e = new Employer();
            e.setEmail(email);
            e.setCompanyName(dto != null ? dto.getOrganizationName() : null);
            e.setVerified(false);
            return employerRepository.save(e);
        });
    }
}

