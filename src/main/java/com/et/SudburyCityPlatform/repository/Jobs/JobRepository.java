package com.et.SudburyCityPlatform.repository.Jobs;

import com.et.SudburyCityPlatform.models.jobs.ApplicationSummary;
import com.et.SudburyCityPlatform.models.jobs.Job;
import com.et.SudburyCityPlatform.models.jobs.JobApplicationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployerId(Long employerId);

    List<Job> findTop20ByOrderByPostedDateDesc();

    @Query("""
SELECT j FROM Job j
WHERE (:location IS NULL OR j.location = :location)
AND (:type IS NULL OR j.employmentType = :type)
AND (:minSalary IS NULL OR j.salary >= :minSalary)
""")
    List<Job> search(
            @Param("location") String location,
            @Param("type") String type,
            @Param("minSalary") Double minSalary
    );

}
