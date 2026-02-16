package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private String name;
    private String description;
    private Boolean currentlyWorking;
    private String startDate;
    private String endDate;
    private String photoUrl;
}
