package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EmpExitFullFinalSettlementDetailsDTO {
    private Integer settlementDetailsId;
    private Integer settlementId;
    private String item;
    private Double amount;
    private String transactionType;
    private LocalDate date;
    private String remarks;
}
