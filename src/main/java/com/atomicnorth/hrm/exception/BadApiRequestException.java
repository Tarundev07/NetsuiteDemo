package com.atomicnorth.hrm.exception;

public class BadApiRequestException extends RuntimeException {

    public BadApiRequestException(String message) {
        super(message);
    }

    public BadApiRequestException() {
        super("Bad Request !!");
    }

    public BadApiRequestException(String s, Exception ex) {
    }
}