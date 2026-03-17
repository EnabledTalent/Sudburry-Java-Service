package com.et.SudburyCityPlatform.repository.serviceprovider;

import com.et.SudburyCityPlatform.models.serviceprovider.ServiceProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceProviderProfileRepository extends JpaRepository<ServiceProviderProfile, Long> {
    Optional<ServiceProviderProfile> findByEmail(String email);
    boolean existsByEmail(String email);
}
