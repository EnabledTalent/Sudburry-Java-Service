package com.et.SudburyCityPlatform.repository.serviceprovider;

import com.et.SudburyCityPlatform.models.serviceprovider.ServiceProviderProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceProviderProfileRepository extends JpaRepository<ServiceProviderProfile, Long> {
    List<ServiceProviderProfile> findAllByEmailOrderByCreatedAtDesc(String email);
}
