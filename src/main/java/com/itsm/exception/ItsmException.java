package com.itsm.exception;

/**
 * Base class for all custom exceptions in the application.
 * Add specific exceptions (InvalidStateTransitionException, TechnicianUnavailableException, ...)
 * by extending this class.
 */
public class ItsmException extends RuntimeException {

    public ItsmException(String message) {
        super(message);
    }

    public ItsmException(String message, Throwable cause) {
        super(message, cause);
    }
}
