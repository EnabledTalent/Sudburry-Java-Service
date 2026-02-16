package com.et.SudburyCityPlatform.exception;

/**
 * Thrown when the user is not allowed to access the resource (e.g. wrong employer). Returns 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
