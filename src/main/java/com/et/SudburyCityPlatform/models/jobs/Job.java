package com.et.SudburyCityPlatform.models.jobs;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String role;

    @Column(columnDefinition = "TEXT")
    private String location;
    private Double salary; // dollars (legacy single value)
    private Double salaryMin;
    private Double salaryMax;
    @Column(columnDefinition = "TEXT")
    private String employmentType; // PART_TIME or FULL_TIME

    @Column(columnDefinition = "TEXT")
    private String requirements;

    private LocalDate postedDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String typeOfWork;

    @Column(columnDefinition = "TEXT")
    private String address;

    /**
     * e.g. "1-2", "2-3", "3-5", "5+"
     */
    @Column(columnDefinition = "TEXT")
    private String experienceRange;

    @Column(columnDefinition = "TEXT")
    private String preferredLanguage;

    private Boolean urgentlyHiring;

    @Column(columnDefinition = "TEXT")
    private String companyName;

    /**
     * When set: job seeker is redirected to this URL on "Apply" (external apply).
     * When null/blank: easy apply (in-app form).
     */
    @Column(name = "external_apply_url", columnDefinition = "TEXT")
    private String externalApplyUrl;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "employer_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_job_employer"),
            nullable=false
    )
    private Employer employer;

    /**
     * Computed per-request for a given job seeker (not stored in DB).
     * Only included in JSON when non-null.
     */
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer matchPercentage;

    public Employer getEmployer() {
        return employer;
    }

    public void setEmployer(Employer employer) {
        this.employer = employer;
    }

    public Integer getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(Integer matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    // Constructors
    public Job() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public LocalDate getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(LocalDate postedDate) {
        this.postedDate = postedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeOfWork() {
        return typeOfWork;
    }

    public void setTypeOfWork(String typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

    public Double getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Double salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Double getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Double salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getExperienceRange() {
        return experienceRange;
    }

    public void setExperienceRange(String experienceRange) {
        this.experienceRange = experienceRange;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public Boolean getUrgentlyHiring() {
        return urgentlyHiring;
    }

    public void setUrgentlyHiring(Boolean urgentlyHiring) {
        this.urgentlyHiring = urgentlyHiring;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getExternalApplyUrl() {
        return externalApplyUrl;
    }

    public void setExternalApplyUrl(String externalApplyUrl) {
        this.externalApplyUrl = externalApplyUrl;
    }
}
