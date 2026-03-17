package com.et.SudburyCityPlatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProviderProfileRequestDTO {

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    private String providerType;
    private String description;
    private String programsAndServices;
    private String contactPreferences;
    private String impactReportingPreferences;
    private String address;
    private String website;
    private String phone;
    private String contactEmail;
}
