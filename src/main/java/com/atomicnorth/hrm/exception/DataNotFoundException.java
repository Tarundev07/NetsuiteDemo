package com.atomicnorth.hrm.exception;

public class DataNotFoundException extends RuntimeException {

    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException() {
        super("Bad Request !!");
    }

    public DataNotFoundException(String s, Exception ex) {
    }
}
