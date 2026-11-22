package com.anootations.farmerseedadvisory.exception;

/**
 * Thrown when a farmer profile is not found in the database.
 */
public class FarmerNotFoundException extends RuntimeException {

    public FarmerNotFoundException(String message) {
        super(message);
    }
}
