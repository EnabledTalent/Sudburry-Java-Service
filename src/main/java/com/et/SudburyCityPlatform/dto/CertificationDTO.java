package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDTO {
    private String name;
    private String issueDate;
    private String issuedOrganization;
    private String credentialId;
    private String credentialUrl;
}
