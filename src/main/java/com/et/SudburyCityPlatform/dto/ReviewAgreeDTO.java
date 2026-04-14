package com.et.SudburyCityPlatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewAgreeDTO {
    private String discovery;
    private String comments;
    private Boolean agreed;
    private Boolean hasDisability;
    private DisabilityDetailsDTO disability;
}
