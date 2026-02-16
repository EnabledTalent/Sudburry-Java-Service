package com.et.SudburyCityPlatform.repository.Jobs;

import com.et.SudburyCityPlatform.models.jobs.JobInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobInviteRepository extends JpaRepository<JobInvite, Long> {

    List<JobInvite> findByJobId(Long jobId);

    List<JobInvite> findByInviteeEmailOrderByInvitedAtDesc(String inviteeEmail);

    boolean existsByJobIdAndInviteeEmail(Long jobId, String inviteeEmail);
}
