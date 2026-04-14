package com.et.SudburyCityPlatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisabilityDetailsDTO {
    private String identifiesAs;
    private List<String> disabilityTypes;
    private String disabilityTypesOther;
    private String workplaceAccommodations;
    private List<String> accommodationSupport;
    private String accommodationSupportOther;
    private Boolean platformAccessibilityNeeded;
    private List<String> platformAccessibilityFeatures;
    private String platformAccessibilityOther;
    private String discussWithHr;
}
