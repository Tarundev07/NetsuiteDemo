package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmpExitKtHandoverDTO {
    private String employeeName;
    private Integer id;
    private String exitRequestNumber;
    private String status;
    private Integer employeeId;
    private String Department;
    private String Designation;
    private String ReportingManager;
    private LocalDate LastWorkingDate;
    private LocalDate handoverStartDate;
    private LocalDate handoverEndDate;

    // Nested details
 private List<EmpExitKtHandoverDetailDTO> details;
}
