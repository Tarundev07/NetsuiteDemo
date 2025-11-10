package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import java.time.LocalDate;

import lombok.Data;

@Data

public class KtHandOverDto {
    private String exitRequestNumber;
    private  int  exitRequestId;
    private Integer employeeId;
    private String employeeName;
    private String  departmentName;
    private LocalDate lastWorkingDay;
    private String status;
}
