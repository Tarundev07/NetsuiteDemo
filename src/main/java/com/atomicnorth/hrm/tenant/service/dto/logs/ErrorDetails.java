package com.atomicnorth.hrm.tenant.service.dto.logs;

import lombok.Data;

@Data
public class ErrorDetails {
    private int logType;
    private String functionName;
    private String className;
    private String moduleCode;
    private String ipInformation;
    private String userId;
}
