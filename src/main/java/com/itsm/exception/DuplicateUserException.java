package com.itsm.exception;

public class DuplicateUserException extends ItsmException {
    public DuplicateUserException(String message) {
        super(message);
    }
}