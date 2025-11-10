package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EmpExitRequestDTO {

    private Integer id;
    private String exitRequestNumber;

    private Integer employeeId;
    private String employeeName;
    private String email;
    private String designation;
    private String department;
    private String reportingManager;
    private String hrManager;
    private LocalDate dateOfJoining;
    private Integer noticePeriod;
    private Boolean ApprovalRequired;

    private LocalDate effectiveFrom;
    private String exitType;
    private String exitReason;
    private LocalDate lastWorkingDate;
    private Boolean requestMeeting;
    private Boolean eligibleToRehire;
    private Boolean requestBuyout;
    private String remarks;
    private String status;
    private String attachment;

    // Child DTOs
    private List<EmpExitApprovalDTO> approvals;
    private List<EmpExitFinanceClearanceDTO> financeClearances;
    private List<EmpExitKtHandoverDTO> ktHandovers;
    private List<EmpExitAdminClearanceDTO> adminClearances;
    private EmpExitInterviewDTO interview;
    private EmpExitFullFinalSettlementDTO settlement;

}
