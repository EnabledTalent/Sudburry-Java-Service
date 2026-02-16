package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceDTO {
    private String jobTitle;
    private String company;
    private String location;
    private String startDate;
    private String endDate;
    private Boolean currentlyWorking;
    private List<String> responsibilities;
    private List<String> technologies;
    private String description;
}
