package com.atomicnorth.hrm.exception;

public class HttpMessageNotReadableException extends RuntimeException {
    public HttpMessageNotReadableException() {
        super("Invalid JSON format not found !!");
    }

    public HttpMessageNotReadableException(String message) {
        super(message);
    }
}
