package com.et.SudburyCityPlatform.models.jobs;


import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "applicantDetails")
@Data
@NoArgsConstructor
public class JobApplicationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phoneNumber;

    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    // Employment Information
    @NotBlank
    private String positionAppliedFor;

    private Integer yearsOfExperience;

    private String highestEducation;

    private String resumeUrl; // or base64 if uploading

    private String linkedInProfile;
    private String githubProfile;
    private String portfolioUrl;

    // Work Authorization
    @NotNull
    private Boolean authorizedToWork;

    @NotNull
    private Boolean requireSponsorship;

    // Equal Employment Opportunity (EEO)
    private Boolean hasDisability;

    private String disabilityDescription;

    private String gender;
    private String raceEthnicity;
    private String veteranStatus;

    // Additional
    private String coverLetter;
    private String additionalInformation;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    /**
     * Computed per-request for a given job (not stored in DB).
     * Only included in JSON when non-null.
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer matchPercentage;

    @PrePersist
    protected void onApply() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }

    public JobApplicationRequest(Long id, Job job, String firstName, String lastName, String email, String phoneNumber, String address, String city, String state, String country, String zipCode, String positionAppliedFor, Integer yearsOfExperience, String highestEducation, String resumeUrl, String linkedInProfile, String githubProfile, String portfolioUrl, Boolean authorizedToWork, Boolean requireSponsorship, Boolean hasDisability, String disabilityDescription, String gender, String raceEthnicity, String veteranStatus, String coverLetter, String additionalInformation, ApplicationStatus status) {
        this.id = id;
        this.job = job;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
        this.positionAppliedFor = positionAppliedFor;
        this.yearsOfExperience = yearsOfExperience;
        this.highestEducation = highestEducation;
        this.resumeUrl = resumeUrl;
        this.linkedInProfile = linkedInProfile;
        this.githubProfile = githubProfile;
        this.portfolioUrl = portfolioUrl;
        this.authorizedToWork = authorizedToWork;
        this.requireSponsorship = requireSponsorship;
        this.hasDisability = hasDisability;
        this.disabilityDescription = disabilityDescription;
        this.gender = gender;
        this.raceEthnicity = raceEthnicity;
        this.veteranStatus = veteranStatus;
        this.coverLetter = coverLetter;
        this.additionalInformation = additionalInformation;
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPositionAppliedFor() {
        return positionAppliedFor;
    }

    public void setPositionAppliedFor(String positionAppliedFor) {
        this.positionAppliedFor = positionAppliedFor;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public void setHighestEducation(String highestEducation) {
        this.highestEducation = highestEducation;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public void setResumeUrl(String resumeUrl) {
        this.resumeUrl = resumeUrl;
    }

    public String getLinkedInProfile() {
        return linkedInProfile;
    }

    public void setLinkedInProfile(String linkedInProfile) {
        this.linkedInProfile = linkedInProfile;
    }

    public String getGithubProfile() {
        return githubProfile;
    }

    public void setGithubProfile(String githubProfile) {
        this.githubProfile = githubProfile;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    public Boolean getAuthorizedToWork() {
        return authorizedToWork;
    }

    public void setAuthorizedToWork(Boolean authorizedToWork) {
        this.authorizedToWork = authorizedToWork;
    }

    public Boolean getRequireSponsorship() {
        return requireSponsorship;
    }

    public void setRequireSponsorship(Boolean requireSponsorship) {
        this.requireSponsorship = requireSponsorship;
    }

    public Boolean getHasDisability() {
        return hasDisability;
    }

    public void setHasDisability(Boolean hasDisability) {
        this.hasDisability = hasDisability;
    }

    public String getDisabilityDescription() {
        return disabilityDescription;
    }

    public void setDisabilityDescription(String disabilityDescription) {
        this.disabilityDescription = disabilityDescription;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRaceEthnicity() {
        return raceEthnicity;
    }

    public void setRaceEthnicity(String raceEthnicity) {
        this.raceEthnicity = raceEthnicity;
    }

    public String getVeteranStatus() {
        return veteranStatus;
    }

    public void setVeteranStatus(String veteranStatus) {
        this.veteranStatus = veteranStatus;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}

