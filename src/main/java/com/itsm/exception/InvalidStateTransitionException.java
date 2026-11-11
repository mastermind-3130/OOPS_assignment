package com.itsm.exception;
/**/
public class InvalidStateTransitionException extends ItsmException{

    public InvalidStateTransitionException(String message){
        super(message);
    }
    public InvalidStateTransitionException(String message, Throwable cause){
        super(message, cause);
    }

}