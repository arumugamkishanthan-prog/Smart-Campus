package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super("The referenced " + resourceType + " with ID '" + resourceId
                + "' does not exist. Please provide a valid reference.");
    }
}
