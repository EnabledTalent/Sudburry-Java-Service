package com.et.SudburyCityPlatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps to the "Post a Job" employer form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployerJobPostRequestDTO {

    @NotBlank
    private String role;

    private String companyName;

    private String jobLocation;

    private String address;

    /**
     * e.g. "1-2", "2-3", "3-5", "5+"
     */
    private String experienceRange;

    /**
     * e.g. "Full time", "Part time", "Internship", "Contract", "Hourly based"
     */
    private String employmentType;

    /**
     * e.g. "Remote", "Hybrid", "Onsite"
     */
    private String typeOfWork;

    private String preferredLanguage;

    private Boolean urgentlyHiring;

    /**
     * Long free-text description.
     */
    private String jobDescription;

    /**
     * Optional: requirements / responsibilities, can be long.
     */
    private String requirements;

    private Double salaryMin;
    private Double salaryMax;

    /**
     * Optional. When set, job seeker is redirected to this URL on "Apply" (external apply).
     * When null/blank, use easy apply (in-app form).
     */
    private String externalApplyUrl;
}

