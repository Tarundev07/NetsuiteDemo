package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class EmpExitApprovalDTO {
    private Integer id;
    private Integer approverId;
    private Integer exitRequestId;
    private String  exitRequestNumber;
    private LocalDate exitInitiateDate;

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
    private String empRemarks;
    private String status;
    private String attachment;

    private String approvalStatus;
    private LocalDate approvalDate;
    private String remarks;
    private Instant CreatedOn;
}
