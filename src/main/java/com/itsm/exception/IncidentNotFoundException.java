package com.itsm.exception;

public class IncidentNotFoundException extends ItsmException{
    public IncidentNotFoundException(String message){
        super(message);
    }
    public IncidentNotFoundException(String message, Throwable cause){
        super(message, cause);
    }

}