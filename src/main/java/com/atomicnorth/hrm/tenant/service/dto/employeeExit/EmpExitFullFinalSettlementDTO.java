package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmpExitFullFinalSettlementDTO {
    private Integer id;
    private Double payableAmount;
    private Double deductions;
    private Double netAmount;
    private String settlementStatus;
    private Integer processedBy;
    private LocalDate processedDate;
    private String remarks;
    private String attachment;
    private Integer exitRequestId;
    private String exitRequestNumber;
    private Integer employeeId;
    private String employeeName;
    private String overallStatus;
    private String departmentName;
    private String designationName;
    private String reportingManager;
    private LocalDate lastWorkingDate;
    private String assetClearanceStatus;
    private String financeClearanceStatus;
    private String ktHandoverStatus;
    private String adminClearanceStatus;
    private String exitInterviewStatus;
    private List<EmpExitFullFinalSettlementDetailsDTO> settlementDetailsDTOS;
}
