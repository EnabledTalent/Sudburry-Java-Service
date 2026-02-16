package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Apply-to-job request: same structure as JobSeekerProfile / ProfileRequestDTO, except no summary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyJobRequestDTO {

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

    // From profile (not in ProfileRequestDTO but needed for apply)
    private Integer yearsOfExperience;
    private String resumeUrl;
    private String city;
    private String postalCode;
}
