package com.melodyguard.api.exception;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(String msg){
        super(msg);
    }
}
