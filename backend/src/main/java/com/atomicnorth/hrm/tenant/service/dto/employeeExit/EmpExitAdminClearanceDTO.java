package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmpExitAdminClearanceDTO {
    private Integer id;
    private Integer exitRequestId;
    private Integer employeeId;
    private String employeeName;
    private String exitRequestNumber;
    private LocalDate lastWorkingDate;
    private String status;
    private String item;
    private String clearanceStatus;
    private Integer clearedBy;
    private LocalDate clearedDate;
    private String remarks;
    private String attachment;
}
