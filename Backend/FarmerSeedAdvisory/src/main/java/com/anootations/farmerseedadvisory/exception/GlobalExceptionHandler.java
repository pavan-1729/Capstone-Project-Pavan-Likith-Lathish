package com.anootations.farmerseedadvisory.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler — converts exceptions into structured
 * JSON error responses with consistent shape:
 *
 * {
 *   "timestamp": "...",
 *   "status": 400,
 *   "error": "VALIDATION_ERROR",
 *   "message": "...",
 *   "path": "/api/farmer/register"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ----------------------------------------------------------------
    // Farmer not found → 404
    // ----------------------------------------------------------------

    @ExceptionHandler(FarmerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFarmerNotFound(
            FarmerNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Farmer not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        HttpStatus.NOT_FOUND,
                        "FARMER_NOT_FOUND",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    // ----------------------------------------------------------------
    // Farmer already exists → 409 Conflict
    // ----------------------------------------------------------------

    @ExceptionHandler(FarmerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleFarmerAlreadyExists(
            FarmerAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Farmer already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(
                        HttpStatus.CONFLICT,
                        "FARMER_ALREADY_EXISTS",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    // ----------------------------------------------------------------
    // Bean Validation (@Valid) failures → 400
    // ----------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Request validation failed: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_ERROR",
                        details,
                        request.getRequestURI()
                ));
    }

    // ----------------------------------------------------------------
    // Business validation error → 400
    // ----------------------------------------------------------------

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_ERROR",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    // ----------------------------------------------------------------
    // Unexpected errors → 500
    // ----------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI()
                ));
    }

    // ----------------------------------------------------------------
    // Error response record
    // ----------------------------------------------------------------

    public static class ErrorResponse {

        private final String timestamp;
        private final int status;
        private final String error;
        private final String message;
        private final String path;

        private ErrorResponse(int status, String error, String message, String path) {
            this.timestamp = LocalDateTime.now().toString();
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        static ErrorResponse of(HttpStatus httpStatus, String error, String message, String path) {
            return new ErrorResponse(httpStatus.value(), error, message, path);
        }

        public String getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
    }
}
