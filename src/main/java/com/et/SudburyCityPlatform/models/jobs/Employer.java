package com.et.SudburyCityPlatform.models.jobs;

import jakarta.persistence.*;

@Entity
@Table(name = "employers")
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    @Column(unique = true)
    private String email;
    private Boolean verified = false;

    public Employer() {
    }

    public Employer(Long id, String companyName, String email, Boolean verified) {
        this.id = id;
        this.companyName = companyName;
        this.email = email;
        this.verified = verified;
    }

    public Employer(Long employerId) {
        this.id = employerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
