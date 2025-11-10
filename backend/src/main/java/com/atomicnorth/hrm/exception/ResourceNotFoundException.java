package com.atomicnorth.hrm.exception;

import lombok.Builder;

@Builder
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Resource not found !!");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String s, String levelcode, String levelvalue) {
    }
}