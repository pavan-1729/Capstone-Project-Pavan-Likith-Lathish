package com.anootations.farmerseedadvisory.exception;

/**
 * Thrown for business-level validation failures.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
