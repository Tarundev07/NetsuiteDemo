package com.atomicnorth.hrm.exception.employement;

public class EmployeeAssignmentNotFoundException extends RuntimeException {
    public EmployeeAssignmentNotFoundException(String message) {
        super(message);
    }
}