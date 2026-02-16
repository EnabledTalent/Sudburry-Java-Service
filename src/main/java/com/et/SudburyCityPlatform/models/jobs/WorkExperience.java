package com.et.SudburyCityPlatform.models.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "work_experience")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    @JsonIgnore
    private JobSeekerProfile profile;

    private String jobTitle;
    private String company;
    private String location;
    private String startDate;
    private String endDate;
    private Boolean currentlyWorking;

    @ElementCollection
    @CollectionTable(name = "work_experience_responsibilities", joinColumns = @JoinColumn(name = "work_experience_id"))
    @Column(name = "responsibility")
    private List<String> responsibilities;

    @ElementCollection
    @CollectionTable(name = "work_experience_technologies", joinColumns = @JoinColumn(name = "work_experience_id"))
    @Column(name = "technology")
    private List<String> technologies;

    @Column(columnDefinition = "TEXT")
    private String description;
}
