package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.dto.ProfileRequestDTO;
import com.et.SudburyCityPlatform.service.Jobs.JobSeekerProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobseeker/profile")
public class JobSeekerProfileController {

    private final JobSeekerProfileService service;

    public JobSeekerProfileController(JobSeekerProfileService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ProfileRequestDTO save(
            @RequestParam String email,
            @RequestBody ProfileRequestDTO request) {
        return service.toDto(service.save(email, request));
    }

    @PutMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ProfileRequestDTO update(
            @RequestParam String email,
            @RequestBody ProfileRequestDTO request) {
        return service.toDto(service.update(email, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT','EMPLOYER')")
    public ProfileRequestDTO get(@RequestParam String email) {
        return service.toDto(service.get(email));
    }

    /**
     * List all job seeker profiles.
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYER')")
    public List<com.et.SudburyCityPlatform.models.jobs.JobSeekerProfile> listAll() {
        return service.listAll();
    }

    @GetMapping("/completion")
    @PreAuthorize("hasRole('STUDENT')")
    public int completion(@RequestParam String email) {
        return service.completion(email);
    }
}


