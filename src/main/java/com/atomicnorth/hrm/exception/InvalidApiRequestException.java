package com.atomicnorth.hrm.exception;


public class InvalidApiRequestException extends RuntimeException {
    public InvalidApiRequestException(String message) {
        super(message);
    }

    public InvalidApiRequestException() {
        super("InvalidApiRequestException");
    }
}