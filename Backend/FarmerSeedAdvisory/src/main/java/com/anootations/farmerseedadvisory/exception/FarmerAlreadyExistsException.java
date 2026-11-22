package com.anootations.farmerseedadvisory.exception;

/**
 * Thrown when a farmer attempts to register with a Firebase UID
 * that already has an existing profile in the database.
 */
public class FarmerAlreadyExistsException extends RuntimeException {

    public FarmerAlreadyExistsException(String message) {
        super(message);
    }
}
