package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationDTO {
    private String degree;
    private String fieldOfStudy;
    private String institution;
    private String startDate;
    private String endDate;
    private String grade;
    private String location;
}
