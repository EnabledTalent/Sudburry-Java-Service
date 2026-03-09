package com.et.SudburyCityPlatform.models.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Preference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    @JsonIgnore
    private JobSeekerProfile profile;

    @ElementCollection
    @CollectionTable(name = "preference_company_sizes", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "company_size")
    private List<String> companySize;

    @ElementCollection
    @CollectionTable(name = "preference_job_types", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "job_type")
    private List<String> jobType;

    @ElementCollection
    @CollectionTable(name = "preference_job_searches", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "job_search")
    private List<String> jobSearch;
}
