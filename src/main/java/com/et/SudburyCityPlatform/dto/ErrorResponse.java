package com.et.SudburyCityPlatform.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard error body returned by the API so the UI can display the message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String message;
    private int status;
    private String error;  // e.g. "Conflict", "Not Found"
    private Instant timestamp;

    public ErrorResponse(String message, int status, String error) {
        this.message = message;
        this.status = status;
        this.error = error;
        this.timestamp = Instant.now();
    }

    public static ErrorResponse of(String message, int status, String error) {
        return new ErrorResponse(message, status, error, Instant.now());
    }
}
