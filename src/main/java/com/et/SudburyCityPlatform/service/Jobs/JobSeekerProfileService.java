package com.et.SudburyCityPlatform.service.Jobs;

import com.et.SudburyCityPlatform.dto.*;
import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.jobs.*;
import com.et.SudburyCityPlatform.repository.Jobs.JobSeekerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class JobSeekerProfileService {

    private final JobSeekerProfileRepository repo;

    public JobSeekerProfileService(JobSeekerProfileRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public JobSeekerProfile save(String email, ProfileRequestDTO dto) {
        JobSeekerProfile p = repo.findByEmail(email).orElse(new JobSeekerProfile());
        applyBasicInfo(email, dto, p);
        // Flush first so user_profiles row exists (ID assigned) before inserting children.
        JobSeekerProfile saved = repo.saveAndFlush(p);
        applyDetails(dto, saved);
        return repo.save(saved);
    }

    /**
     * Update an existing profile. Throws 404 if the profile doesn't exist.
     */
    @Transactional
    public JobSeekerProfile update(String email, ProfileRequestDTO dto) {
        JobSeekerProfile p = repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        applyBasicInfo(email, dto, p);
        JobSeekerProfile saved = repo.saveAndFlush(p);
        applyDetails(dto, saved);
        return repo.save(saved);
    }

    /**
     * Admin-only use-case: list all job seeker profiles.
     */
    public List<JobSeekerProfile> listAll() {
        return repo.findAll();
    }

    private void applyBasicInfo(String email, ProfileRequestDTO dto, JobSeekerProfile p) {
        p.setEmail(email);

        if (dto.getBasicInfo() != null) {
            p.setFullName(dto.getBasicInfo().getName());
            p.setPhone(dto.getBasicInfo().getPhone());
            p.setLinkedin(dto.getBasicInfo().getLinkedin());
        }
        // fullName is non-null in DB; avoid null if client omits it
        if (p.getFullName() == null) p.setFullName("");
    }

    private void applyDetails(ProfileRequestDTO dto, JobSeekerProfile p) {
        p.setSkills(dto.getSkills() != null ? dto.getSkills() : Collections.emptyList());
        p.setPrimarySkills(dto.getPrimarySkills() != null ? dto.getPrimarySkills() : Collections.emptyList());
        p.setBasicSkills(dto.getBasicSkills() != null ? dto.getBasicSkills() : Collections.emptyList());

        // Clear and set one-to-many collections
        if (p.getEducation() != null) p.getEducation().clear();
        else p.setEducation(new ArrayList<>());
        if (dto.getEducation() != null) {
            for (EducationDTO ed : dto.getEducation()) {
                Education e = mapEducation(ed, p);
                p.getEducation().add(e);
            }
        }

        if (p.getWorkExperience() != null) p.getWorkExperience().clear();
        else p.setWorkExperience(new ArrayList<>());
        if (dto.getWorkExperience() != null) {
            for (WorkExperienceDTO we : dto.getWorkExperience()) {
                p.getWorkExperience().add(mapWorkExperience(we, p));
            }
        }

        if (p.getProjects() != null) p.getProjects().clear();
        else p.setProjects(new ArrayList<>());
        if (dto.getProjects() != null) {
            for (ProjectDTO pd : dto.getProjects()) {
                p.getProjects().add(mapProject(pd, p));
            }
        }

        if (p.getAchievements() != null) p.getAchievements().clear();
        else p.setAchievements(new ArrayList<>());
        if (dto.getAchievements() != null) {
            for (AchievementDTO ad : dto.getAchievements()) {
                p.getAchievements().add(mapAchievement(ad, p));
            }
        }

        if (p.getCertifications() != null) p.getCertifications().clear();
        else p.setCertifications(new ArrayList<>());
        if (dto.getCertification() != null) {
            for (CertificationDTO cd : dto.getCertification()) {
                p.getCertifications().add(mapCertification(cd, p));
            }
        }

        if (p.getLanguages() != null) p.getLanguages().clear();
        else p.setLanguages(new ArrayList<>());
        if (dto.getOtherDetails() != null && dto.getOtherDetails().getLanguages() != null) {
            for (LanguageProficiencyDTO ld : dto.getOtherDetails().getLanguages()) {
                p.getLanguages().add(mapLanguageProficiency(ld, p));
            }
        }

        // One-to-one
        if (dto.getPreference() != null) {
            Preference pref = p.getPreference() != null ? p.getPreference() : new Preference();
            pref.setProfile(p);
            pref.setCompanySize(dto.getPreference().getCompanySize());
            pref.setJobType(dto.getPreference().getJobType());
            pref.setJobSearch(dto.getPreference().getJobSearch());
            p.setPreference(pref);
        }

        if (dto.getOtherDetails() != null) {
            OtherDetails od = p.getOtherDetails() != null ? p.getOtherDetails() : new OtherDetails();
            od.setProfile(p);
            od.setCareerStage(dto.getOtherDetails().getCareerStage());
            od.setEarliestAvailability(dto.getOtherDetails().getEarliestAvailability());
            od.setDesiredSalary(dto.getOtherDetails().getDesiredSalary());
            od.setOtherDetailsText(dto.getOtherDetails().getOtherDetailsText());
            p.setOtherDetails(od);
        }

        if (dto.getReviewAgree() != null) {
            ReviewAgree ra = p.getReviewAgree() != null ? p.getReviewAgree() : new ReviewAgree();
            ra.setProfile(p);
            ra.setDiscovery(dto.getReviewAgree().getDiscovery());
            ra.setComments(dto.getReviewAgree().getComments());
            ra.setAgreed(dto.getReviewAgree().getAgreed());
            p.setReviewAgree(ra);
        }
    }

    @Transactional
    public JobSeekerProfile save(String email, JobSeekerProfile profile) {
        JobSeekerProfile p = repo.findByEmail(email).orElse(new JobSeekerProfile());
        p.setEmail(email);
        p.setFullName(profile.getFullName());
        p.setPhone(profile.getPhone());
        p.setLinkedin(profile.getLinkedin());
        p.setCity(profile.getCity());
        p.setPostalCode(profile.getPostalCode());
        p.setSummary(profile.getSummary());
        p.setSkills(profile.getSkills());
        p.setPrimarySkills(profile.getPrimarySkills());
        p.setBasicSkills(profile.getBasicSkills());
        p.setYearsOfExperience(profile.getYearsOfExperience());
        p.setResumeUrl(profile.getResumeUrl());

        // Flush parent first (ensures ID exists) then attach children to this parent.
        JobSeekerProfile saved = repo.saveAndFlush(p);

        if (profile.getEducation() != null) {
            saved.setEducation(profile.getEducation());
            for (Education e : profile.getEducation()) e.setProfile(saved);
        }
        if (profile.getWorkExperience() != null) {
            saved.setWorkExperience(profile.getWorkExperience());
            for (WorkExperience we : profile.getWorkExperience()) we.setProfile(saved);
        }
        if (profile.getProjects() != null) {
            saved.setProjects(profile.getProjects());
            for (Project pr : profile.getProjects()) pr.setProfile(saved);
        }
        if (profile.getAchievements() != null) {
            saved.setAchievements(profile.getAchievements());
            for (Achievement a : profile.getAchievements()) a.setProfile(saved);
        }
        if (profile.getCertifications() != null) {
            saved.setCertifications(profile.getCertifications());
            for (Certification c : profile.getCertifications()) c.setProfile(saved);
        }
        if (profile.getLanguages() != null) {
            saved.setLanguages(profile.getLanguages());
            for (LanguageProficiency lp : profile.getLanguages()) lp.setProfile(saved);
        }
        if (profile.getPreference() != null) {
            profile.getPreference().setProfile(saved);
            saved.setPreference(profile.getPreference());
        }
        if (profile.getOtherDetails() != null) {
            profile.getOtherDetails().setProfile(saved);
            saved.setOtherDetails(profile.getOtherDetails());
        }
        if (profile.getReviewAgree() != null) {
            profile.getReviewAgree().setProfile(saved);
            saved.setReviewAgree(profile.getReviewAgree());
        }

        return repo.save(saved);
    }

    public JobSeekerProfile get(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    public int completion(String email) {
        JobSeekerProfile p = get(email);
        int filled = 0;
        int total = 14;
        if (p.getFullName() != null && !p.getFullName().isBlank()) filled++;
        if (p.getEmail() != null && !p.getEmail().isBlank()) filled++;
        if (p.getPhone() != null && !p.getPhone().isBlank()) filled++;
        if (p.getCity() != null && !p.getCity().isBlank()) filled++;
        if (p.getSummary() != null && !p.getSummary().isBlank()) filled++;
        if (p.getSkills() != null && !p.getSkills().isEmpty()) filled++;
        if (p.getPrimarySkills() != null && !p.getPrimarySkills().isEmpty()) filled++;
        if (p.getResumeUrl() != null && !p.getResumeUrl().isBlank()) filled++;
        if (p.getYearsOfExperience() != null) filled++;
        if (p.getEducation() != null && !p.getEducation().isEmpty()) filled++;
        if (p.getWorkExperience() != null && !p.getWorkExperience().isEmpty()) filled++;
        if (p.getPreference() != null) filled++;
        if (p.getOtherDetails() != null) filled++;
        if (p.getReviewAgree() != null && Boolean.TRUE.equals(p.getReviewAgree().getAgreed())) filled++;
        return total > 0 ? (filled * 100) / total : 0;
    }

    private static Education mapEducation(EducationDTO dto, JobSeekerProfile profile) {
        Education e = new Education();
        e.setProfile(profile);
        e.setDegree(dto.getDegree());
        e.setFieldOfStudy(dto.getFieldOfStudy());
        e.setInstitution(dto.getInstitution());
        e.setStartDate(dto.getStartDate());
        e.setEndDate(dto.getEndDate());
        e.setGrade(dto.getGrade());
        e.setLocation(dto.getLocation());
        return e;
    }

    private static WorkExperience mapWorkExperience(WorkExperienceDTO dto, JobSeekerProfile profile) {
        WorkExperience we = new WorkExperience();
        we.setProfile(profile);
        we.setJobTitle(dto.getJobTitle());
        we.setCompany(dto.getCompany());
        we.setLocation(dto.getLocation());
        we.setStartDate(dto.getStartDate());
        we.setEndDate(dto.getEndDate());
        we.setCurrentlyWorking(dto.getCurrentlyWorking());
        we.setResponsibilities(dto.getResponsibilities());
        we.setTechnologies(dto.getTechnologies());
        we.setDescription(dto.getDescription());
        return we;
    }

    private static Project mapProject(ProjectDTO dto, JobSeekerProfile profile) {
        Project pr = new Project();
        pr.setProfile(profile);
        pr.setName(dto.getName());
        pr.setDescription(dto.getDescription());
        pr.setCurrentlyWorking(dto.getCurrentlyWorking());
        pr.setStartDate(dto.getStartDate());
        pr.setEndDate(dto.getEndDate());
        pr.setPhotoUrl(dto.getPhotoUrl());
        return pr;
    }

    private static Achievement mapAchievement(AchievementDTO dto, JobSeekerProfile profile) {
        Achievement a = new Achievement();
        a.setProfile(profile);
        a.setTitle(dto.getTitle());
        a.setIssueDate(dto.getIssueDate());
        a.setDescription(dto.getDescription());
        return a;
    }

    private static Certification mapCertification(CertificationDTO dto, JobSeekerProfile profile) {
        Certification c = new Certification();
        c.setProfile(profile);
        c.setName(dto.getName());
        c.setIssueDate(dto.getIssueDate());
        c.setIssuedOrganization(dto.getIssuedOrganization());
        c.setCredentialId(dto.getCredentialId());
        c.setCredentialUrl(dto.getCredentialUrl());
        return c;
    }

    private static LanguageProficiency mapLanguageProficiency(LanguageProficiencyDTO dto, JobSeekerProfile profile) {
        LanguageProficiency lp = new LanguageProficiency();
        lp.setProfile(profile);
        lp.setLanguage(dto.getLanguage());
        lp.setSpeaking(dto.getSpeaking());
        lp.setReading(dto.getReading());
        lp.setWriting(dto.getWriting());
        return lp;
    }
}
