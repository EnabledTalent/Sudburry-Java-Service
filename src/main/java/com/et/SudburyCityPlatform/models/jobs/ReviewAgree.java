package com.et.SudburyCityPlatform.models.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_agrees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAgree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    @JsonIgnore
    private JobSeekerProfile profile;

    private String discovery;
    private String comments;
    private Boolean agreed;
    private Boolean hasDisability;

    /**
     * Full disability questionnaire from the UI (JSON). Stored as text/jsonb via ddl-auto.
     */
    @Column(name = "disability_json", columnDefinition = "TEXT")
    private String disabilityJson;
}
