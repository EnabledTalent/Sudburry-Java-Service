package com.et.SudburyCityPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewAgreeDTO {
    private String discovery;
    private String comments;
    private Boolean agreed;
}
