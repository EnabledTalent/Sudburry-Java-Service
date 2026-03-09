package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerNotificationsResponseDTO {
    private List<JobSeekerInviteDTO> invites;
    private List<JobSeekerNotificationDTO> recruiterActions;
    private List<JobMatchDTO> recommendedJobs; // matchPercentage computed per job seeker
}

