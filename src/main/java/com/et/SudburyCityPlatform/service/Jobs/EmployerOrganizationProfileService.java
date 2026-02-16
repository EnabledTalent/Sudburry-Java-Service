package com.et.SudburyCityPlatform.service.Jobs;

import com.et.SudburyCityPlatform.dto.EmployerOrganizationProfileRequestDTO;
import com.et.SudburyCityPlatform.exception.ConflictException;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.jobs.Employer;
import com.et.SudburyCityPlatform.models.jobs.EmployerOrganizationProfile;
import com.et.SudburyCityPlatform.repository.Jobs.EmployerRepository;
import com.et.SudburyCityPlatform.repository.Jobs.EmployerOrganizationProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class EmployerOrganizationProfileService {

    private final EmployerOrganizationProfileRepository repo;
    private final EmployerRepository employerRepository;

    public EmployerOrganizationProfileService(
            EmployerOrganizationProfileRepository repo,
            EmployerRepository employerRepository
    ) {
        this.repo = repo;
        this.employerRepository = employerRepository;
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

    public void delete(String email) {
        EmployerOrganizationProfile p = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employer organization profile not found"));
        repo.delete(p);
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

