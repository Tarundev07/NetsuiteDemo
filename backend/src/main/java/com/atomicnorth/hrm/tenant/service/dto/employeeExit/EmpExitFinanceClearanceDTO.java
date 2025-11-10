package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmpExitFinanceClearanceDTO {
    private Integer id;
    private Double outstandingAmount;
    private Double deductions;
    private Double reimbursements;
    private Double finalPayable;
    private String clearanceStatus;
    private String remarks;
    private Integer clearedBy;
    private LocalDate clearedDate;
}
