package com.et.SudburyCityPlatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerAiChatRequestDTO {

    /**
     * Optional if you use JWT; if omitted we'll use Authentication.getName().
     */
    @Email
    private String email;

    @NotBlank
    private String message;

    /**
     * Optional tuning knobs to keep context small.
     */
    private Integer maxAvailableJobs;
    private Integer maxAppliedJobs;
}

