package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FinanceClearanceDetailsDTO {
    private Integer financeDetailsId;
    private Integer financeClearanceId;
    private String itemType;
    private Double itemAmount;
    private String status;
    private String remarks;
    private LocalDate date;
    private String document;
}
