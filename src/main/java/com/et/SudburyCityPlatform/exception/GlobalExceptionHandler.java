package com.et.SudburyCityPlatform.exception;

import com.et.SudburyCityPlatform.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Returns proper JSON error responses with status and message so the UI can display them.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyAppliedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyApplied(AlreadyAppliedException e) {
        ErrorResponse body = ErrorResponse.of(e.getMessage(), HttpStatus.CONFLICT.value(), "Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException e) {
        ErrorResponse body = ErrorResponse.of(e.getMessage(), HttpStatus.CONFLICT.value(), "Conflict");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        ErrorResponse body = ErrorResponse.of(e.getMessage(), HttpStatus.NOT_FOUND.value(), "Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e) {
        ErrorResponse body = ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value(), "Bad Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
        ErrorResponse body = ErrorResponse.of(e.getMessage(), HttpStatus.FORBIDDEN.value(), "Forbidden");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse body = ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value(), "Bad Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        ErrorResponse body = ErrorResponse.of(
                e.getMessage() != null ? e.getMessage() : "Access Denied",
                HttpStatus.FORBIDDEN.value(),
                "Forbidden"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e) {
        ErrorResponse body = ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value(), "Bad Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e) {
        ErrorResponse body = ErrorResponse.of(
                e.getMessage() != null ? e.getMessage() : "An error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
