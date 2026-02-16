package com.et.SudburyCityPlatform.repository.Jobs;

import com.et.SudburyCityPlatform.models.jobs.ApplicationSummary;
import com.et.SudburyCityPlatform.models.jobs.JobApplicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.et.SudburyCityPlatform.models.jobs.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface JobApplicationRepository
        extends JpaRepository<JobApplicationRequest, Long> {

    List<JobApplicationRequest> findByEmail(String email);

    List<JobApplicationRequest> findByEmailAndStatusIn(String email, List<ApplicationStatus> statuses);

    long countByEmail(String email);

    long countByEmailAndStatus(String email, ApplicationStatus status);

    long countByEmailAndAppliedAtAfter(String email, LocalDateTime after);

    long countByEmailAndStatusAndAppliedAtAfter(String email, ApplicationStatus status, LocalDateTime after);

    List<JobApplicationRequest> findByJobId(Long jobId);

    List<JobApplicationRequest> findByJobIdIn(List<Long> jobIds);

    boolean existsByJobIdAndEmail(Long jobId, String email);
    @Query("""
SELECT a.status AS status, COUNT(a) AS count
FROM JobApplicationRequest a
WHERE a.email = :email
GROUP BY a.status
""")
    List<ApplicationSummary> summary(@Param("email") String email);

}

