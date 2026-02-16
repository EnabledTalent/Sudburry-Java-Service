package com.et.SudburyCityPlatform.exception;

/**
 * Thrown for invalid input (e.g. missing email). Returns 400 Bad Request so UI can show the message.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
