package com.et.SudburyCityPlatform.exception;

/**
 * Thrown for duplicate/conflict cases (already enrolled, already registered). Returns 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
