package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;
@Data
public class AdminClearanceDto {
        private Integer exitRequestId;
        private String exitRequestNumber;
        private Integer employeeId;
        private String Designation;
        private String employeeName;
        private String  departmentName;
        private LocalDate lastWorkingDay;
        private String status;
}
