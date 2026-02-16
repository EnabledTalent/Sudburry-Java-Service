package com.et.SudburyCityPlatform.exception;

/**
 * Thrown when a requested resource (job, application, profile, program, event) is not found. Returns 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
