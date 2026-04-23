package com.smartcampus.exception;

/**
 * Thrown when a client attempts to create a Sensor referencing a roomId that
 * does not exist.
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super("The referenced " + resourceType + " with ID '" + resourceId
                + "' does not exist. Please provide a valid reference.");
    }
}
