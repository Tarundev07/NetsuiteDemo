package com.atomicnorth.hrm.util;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OldApiResponseMessageBuilder<T> {
    private boolean error;  // Renamed from isError to error
    private String responseCode;
    private List<String> tokens;
    private List<T> data;  // Generic type for data
    private List<String> errors;
    private List<String> exceptions;
    private String responseMessageType;
}

