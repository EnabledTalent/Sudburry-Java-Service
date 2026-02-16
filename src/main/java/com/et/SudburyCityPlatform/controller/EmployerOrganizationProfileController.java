package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.dto.EmployerOrganizationProfileRequestDTO;
import com.et.SudburyCityPlatform.models.jobs.EmployerOrganizationProfile;
import com.et.SudburyCityPlatform.service.Jobs.EmployerOrganizationProfileService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer/profile/organization")
@PreAuthorize("hasAnyRole('ADMIN','EMPLOYER')")
public class EmployerOrganizationProfileController {

    private final EmployerOrganizationProfileService service;

    public EmployerOrganizationProfileController(EmployerOrganizationProfileService service) {
        this.service = service;
    }

    @PostMapping
    public EmployerOrganizationProfile create(
            @RequestParam String email,
            @Valid @RequestBody EmployerOrganizationProfileRequestDTO request
    ) {
        return service.create(email, request);
    }

    @PutMapping
    public EmployerOrganizationProfile update(
            @RequestParam String email,
            @Valid @RequestBody EmployerOrganizationProfileRequestDTO request
    ) {
        return service.update(email, request);
    }

    @GetMapping
    public EmployerOrganizationProfile get(@RequestParam String email) {
        return service.get(email);
    }

    @DeleteMapping
    public void delete(@RequestParam String email) {
        service.delete(email);
    }
}

