package com.et.SudburyCityPlatform.models.program;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "programs")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String provider;

    private BigDecimal cost;

    private String duration; // "8 weeks"

    private LocalDate startDate;

    private Integer availableSeats;

    @Column(length = 2000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "program_learning_points", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "point")
    private List<String> learningPoints;

    @ElementCollection
    @CollectionTable(name = "program_eligibility", joinColumns = @JoinColumn(name = "program_id"))
    @Column(name = "criteria")
    private List<String> eligibility;

    @OneToMany(mappedBy = "program")
    private List<ProgramEnrollment> enrollments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getLearningPoints() {
        return learningPoints;
    }

    public void setLearningPoints(List<String> learningPoints) {
        this.learningPoints = learningPoints;
    }

    public List<String> getEligibility() {
        return eligibility;
    }

    public void setEligibility(List<String> eligibility) {
        this.eligibility = eligibility;
    }

    public List<ProgramEnrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<ProgramEnrollment> enrollments) {
        this.enrollments = enrollments;
    }
}

