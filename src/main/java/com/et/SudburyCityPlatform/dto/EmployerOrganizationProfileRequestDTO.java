package com.et.SudburyCityPlatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerOrganizationProfileRequestDTO {

    @NotBlank
    private String organizationName;

    private String aboutOrganization;
    private String location;
    private Integer foundedYear;
    private String website;
    private String companySize;
    private String industry;
}

