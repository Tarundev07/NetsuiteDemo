package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class EmployeeAssetClearance {

    private Integer exitRequestId;
    private String exitRequestNumber;
    private Integer employeeId;
    private String status;
    private LocalDate lastWorkingDay;

    private String  employeeName;
    private String  departmentName;
    private Long  departmentId;



}
