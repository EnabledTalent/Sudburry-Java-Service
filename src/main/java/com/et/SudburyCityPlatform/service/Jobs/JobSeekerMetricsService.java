package com.et.SudburyCityPlatform.service.Jobs;

import com.et.SudburyCityPlatform.dto.JobSeekerMetricsDTO;
import com.et.SudburyCityPlatform.exception.BadRequestException;
import com.et.SudburyCityPlatform.models.jobs.ApplicationStatus;
import com.et.SudburyCityPlatform.repository.Jobs.JobApplicationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
public class JobSeekerMetricsService {

    private final JobApplicationRepository jobApplicationRepository;

    public JobSeekerMetricsService(JobApplicationRepository jobApplicationRepository) {
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public JobSeekerMetricsDTO metrics(String email, Integer windowDays) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("email is required");
        }

        int days = (windowDays == null || windowDays <= 0) ? 90 : windowDays;
        LocalDateTime start = LocalDateTime.now().minusDays(days);

        long total = jobApplicationRepository.countByEmail(email);
        Map<ApplicationStatus, Long> totalByStatus = countsByStatus(email, null);

        long inWindow = jobApplicationRepository.countByEmailAndAppliedAtAfter(email, start);
        Map<ApplicationStatus, Long> windowByStatus = countsByStatus(email, start);

        return new JobSeekerMetricsDTO(
                email,
                total,
                totalByStatus,
                days,
                start,
                inWindow,
                windowByStatus
        );
    }

    private Map<ApplicationStatus, Long> countsByStatus(String email, LocalDateTime start) {
        Map<ApplicationStatus, Long> map = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus s : ApplicationStatus.values()) {
            long c = (start == null)
                    ? jobApplicationRepository.countByEmailAndStatus(email, s)
                    : jobApplicationRepository.countByEmailAndStatusAndAppliedAtAfter(email, s, start);
            map.put(s, c);
        }
        return map;
    }
}

