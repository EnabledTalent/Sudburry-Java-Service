package com.et.SudburyCityPlatform.models.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "other_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtherDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    @JsonIgnore
    private JobSeekerProfile profile;

    private String careerStage;
    private String earliestAvailability;
    private String desiredSalary;

    @Column(columnDefinition = "TEXT")
    private String otherDetailsText;
}
