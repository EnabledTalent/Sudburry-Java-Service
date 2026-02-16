package com.et.SudburyCityPlatform.dto;

import com.et.SudburyCityPlatform.models.jobs.Job;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchDTO {
    private Job job;
    private int matchPercentage;          // 0..100
    private int skillMatchPercentage;     // 0..100
    private int experienceMatchPercentage;// 0..100
    private List<String> matchedSkills;
    private List<String> missingSkills;
}

