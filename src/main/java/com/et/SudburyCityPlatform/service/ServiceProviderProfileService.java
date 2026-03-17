package com.et.SudburyCityPlatform.service;

import com.et.SudburyCityPlatform.dto.ServiceProviderProfileRequestDTO;
import com.et.SudburyCityPlatform.exception.ForbiddenException;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.serviceprovider.ServiceProviderProfile;
import com.et.SudburyCityPlatform.repository.serviceprovider.ServiceProviderProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceProviderProfileService {

    private final ServiceProviderProfileRepository repository;

    public ServiceProviderProfileService(ServiceProviderProfileRepository repository) {
        this.repository = repository;
    }

    public ServiceProviderProfile create(String email, ServiceProviderProfileRequestDTO dto) {
        ServiceProviderProfile profile = new ServiceProviderProfile();
        profile.setEmail(email);
        apply(dto, profile);
        return repository.save(profile);
    }

    public List<ServiceProviderProfile> listByEmail(String email) {
        return repository.findAllByEmailOrderByCreatedAtDesc(email);
    }

    public ServiceProviderProfile update(Long id, String ownerEmail, ServiceProviderProfileRequestDTO dto) {
        ServiceProviderProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider profile not found"));
        if (!profile.getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new ForbiddenException("Unauthorized access");
        }
        apply(dto, profile);
        return repository.save(profile);
    }

    public ServiceProviderProfile getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider profile not found"));
    }

    public void delete(Long id, String ownerEmail) {
        ServiceProviderProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider profile not found"));
        if (!profile.getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new ForbiddenException("Unauthorized access");
        }
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
