package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.dto.ServiceProviderProfileRequestDTO;
import com.et.SudburyCityPlatform.exception.BadRequestException;
import com.et.SudburyCityPlatform.exception.ForbiddenException;
import com.et.SudburyCityPlatform.models.jobs.CustomUserDetails;
import com.et.SudburyCityPlatform.models.serviceprovider.ServiceProviderProfile;
import com.et.SudburyCityPlatform.service.ServiceProviderProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/service-provider/profile")
@PreAuthorize("hasAnyRole('ADMIN','SERVICEPROVIDER')")
@Tag(name = "Service Provider Profile", description = "CRUD for service provider organization profiles")
public class ServiceProviderProfileController {

    private final ServiceProviderProfileService service;

    public ServiceProviderProfileController(ServiceProviderProfileService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create service provider profile")
    public ResponseEntity<ServiceProviderProfile> create(
            @RequestParam(required = false) String email,
            @Valid @RequestBody ServiceProviderProfileRequestDTO request,
            Authentication auth
    ) {
        String resolvedEmail = resolveEmail(email, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(resolvedEmail, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update service provider profile by ID")
    public ServiceProviderProfile update(
            @PathVariable Long id,
            @RequestParam(required = false) String email,
            @Valid @RequestBody ServiceProviderProfileRequestDTO request,
            Authentication auth
    ) {
        String resolvedEmail = resolveEmail(email, auth);
        return service.update(id, resolvedEmail, request);
    }

    @GetMapping
    @Operation(summary = "List all service provider profiles for email")
    public List<ServiceProviderProfile> list(
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        String resolvedEmail = resolveEmail(email, auth);
        return service.listByEmail(resolvedEmail);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service provider profile by ID")
    public ServiceProviderProfile getById(
            @PathVariable Long id,
            Authentication auth
    ) {
        ServiceProviderProfile profile = service.getById(id);
        CustomUserDetails principal = auth != null && auth.getPrincipal() instanceof CustomUserDetails ud ? ud : null;
        if (principal != null && !"ADMIN".equals(principal.getRole())) {
            String authEmail = principal.getUsername();
            if (authEmail == null || !authEmail.equalsIgnoreCase(profile.getEmail() != null ? profile.getEmail().trim() : "")) {
                throw new ForbiddenException("Unauthorized access");
            }
        }
        return profile;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete service provider profile by ID")
    public void delete(
            @PathVariable Long id,
            @RequestParam(required = false) String email,
            Authentication auth
    ) {
        String resolvedEmail = resolveEmail(email, auth);
        service.delete(id, resolvedEmail);
    }

    private String resolveEmail(String email, Authentication auth) {
        CustomUserDetails principal = auth != null && auth.getPrincipal() instanceof CustomUserDetails ud
                ? ud : null;
        String authRole = principal != null ? principal.getRole() : null;

        if (email == null || email.isBlank()) {
            email = principal != null ? principal.getUsername() : null;
        }
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        // SERVICEPROVIDER can only access their own profile; ADMIN can access any
        if (!"ADMIN".equals(authRole)) {
            String authEmail = principal != null ? principal.getUsername() : null;
            if (authEmail == null || !authEmail.equalsIgnoreCase(email.trim())) {
                throw new ForbiddenException("Unauthorized access");
            }
        }
        return email.trim();
    }
}
