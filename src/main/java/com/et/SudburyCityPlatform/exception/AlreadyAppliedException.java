package com.et.SudburyCityPlatform.exception;

/**
 * Thrown when the user has already applied for the job. Returns 409 Conflict so UI can show the message.
 */
public class AlreadyAppliedException extends RuntimeException {

    public AlreadyAppliedException(String message) {
        super(message);
    }
}
