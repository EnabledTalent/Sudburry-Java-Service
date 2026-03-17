package com.et.SudburyCityPlatform.service;

import com.et.SudburyCityPlatform.dto.ServiceProviderProfileRequestDTO;
import com.et.SudburyCityPlatform.exception.ConflictException;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.serviceprovider.ServiceProviderProfile;
import com.et.SudburyCityPlatform.repository.serviceprovider.ServiceProviderProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceProviderProfileService {

    private final ServiceProviderProfileRepository repository;

    public ServiceProviderProfileService(ServiceProviderProfileRepository repository) {
        this.repository = repository;
    }

    public ServiceProviderProfile create(String email, ServiceProviderProfileRequestDTO dto) {
        if (repository.existsByEmail(email)) {
            throw new ConflictException("Service provider profile already exists for this email");
        }
        ServiceProviderProfile profile = new ServiceProviderProfile();
        profile.setEmail(email);
        apply(dto, profile);
        return repository.save(profile);
    }

    public ServiceProviderProfile update(String email, ServiceProviderProfileRequestDTO dto) {
        ServiceProviderProfile profile = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider profile not found"));
        apply(dto, profile);
        return repository.save(profile);
    }

    public ServiceProviderProfile get(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider profile not found"));
    }

    public ServiceProviderProfile getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider profile not found"));
    }

    public void delete(String email) {
        ServiceProviderProfile profile = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider profile not found"));
        repository.delete(profile);
    }

    private void apply(ServiceProviderProfileRequestDTO dto, ServiceProviderProfile profile) {
        profile.setProviderType(dto.getProviderType());
        profile.setOrganizationName(dto.getOrganizationName());
        profile.setDescription(dto.getDescription());
        profile.setProgramsAndServices(dto.getProgramsAndServices());
        profile.setContactPreferences(dto.getContactPreferences());
        profile.setImpactReportingPreferences(dto.getImpactReportingPreferences());
        profile.setAddress(dto.getAddress());
        profile.setWebsite(dto.getWebsite());
        profile.setPhone(dto.getPhone());
        profile.setContactEmail(dto.getContactEmail());
    }
}
