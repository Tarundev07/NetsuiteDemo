package com.atomicnorth.hrm.tenant.service.dto.logs;

import lombok.Data;

@Data
public class LogRequest {
    private ErrorDetails error;
    private String logDate;
    private String message;
    private String source;
    private String stackTrace;
    private String targetSite;
    private String logTable;
    private String jsonData;
}
