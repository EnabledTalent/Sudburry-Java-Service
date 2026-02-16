package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDTO {

    // Basic Info
    private BasicInfoDTO basicInfo;

    // Collections
    private List<EducationDTO> education;
    private List<WorkExperienceDTO> workExperience;
    private List<String> skills;
    private List<String> primarySkills;
    private List<String> basicSkills;
    private List<ProjectDTO> projects;
    private List<AchievementDTO> achievements;
    private List<CertificationDTO> certification;

    // Single Objects
    private PreferenceDTO preference;
    private OtherDetailsDTO otherDetails;
    private ReviewAgreeDTO reviewAgree;
}
