package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SeparationListDTO {

    private Integer id;
    private Integer employeeId;
    private String exitRequestNumber;
    private String exitType;
    private String exitReason;
    private LocalDate lastWorkingDate;
    private String remarks;
    private String status;
    private String employeeName;
}
