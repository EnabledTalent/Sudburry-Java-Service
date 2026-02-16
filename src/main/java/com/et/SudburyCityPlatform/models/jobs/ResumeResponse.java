package com.et.SudburyCityPlatform.models.jobs;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ResumeResponse {

    /** ---------- RAW ---------- */

    /** ---------- BASIC INFO ---------- */
    private Map<String, String> basicInfo;
    // keys: name, email, phone, linkedin

    /** ---------- CORE SECTIONS ---------- */
    private List<Education> education;
    private List<WorkExperience> workExperience;
    private List<String> skills;
    private List<String> projects;
    private List<String> achievements;
    private List<String> certification;

    /** ---------- OPTIONAL / META ---------- */
    private List<String> preference;
    private String otherDetails;
    private List<String> accessibilityNeeds;

    /** ---------- CONSENT ---------- */
    private Boolean reviewAgree;



    public Map<String, String> getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(Map<String, String> basicInfo) {
        this.basicInfo = basicInfo;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education;
    }

    public List<WorkExperience> getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(List<WorkExperience> workExperience) {
        this.workExperience = workExperience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    public List<String> getCertification() {
        return certification;
    }

    public void setCertification(List<String> certification) {
        this.certification = certification;
    }

    public List<String> getPreference() {
        return preference;
    }

    public void setPreference(List<String> preference) {
        this.preference = preference;
    }

    public String getOtherDetails() {
        return otherDetails;
    }

    public void setOtherDetails(String otherDetails) {
        this.otherDetails = otherDetails;
    }

    public List<String> getAccessibilityNeeds() {
        return accessibilityNeeds;
    }

    public void setAccessibilityNeeds(List<String> accessibilityNeeds) {
        this.accessibilityNeeds = accessibilityNeeds;
    }

    public Boolean getReviewAgree() {
        return reviewAgree;
    }

    public void setReviewAgree(Boolean reviewAgree) {
        this.reviewAgree = reviewAgree;
    }
}
