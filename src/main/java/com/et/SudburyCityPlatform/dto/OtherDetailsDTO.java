package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtherDetailsDTO {
    private List<LanguageProficiencyDTO> languages;
    private String careerStage;
    private String earliestAvailability;
    private String desiredSalary;
    private String otherDetailsText;
}
