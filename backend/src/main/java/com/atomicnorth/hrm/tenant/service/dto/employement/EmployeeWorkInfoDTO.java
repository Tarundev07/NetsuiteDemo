package com.atomicnorth.hrm.tenant.service.dto.employement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkInfoDTO {
    private String employeeType;
    private String isActive;
    private String baseLocation;
    private Integer divisionId;
    private Long departmentId;
    private Integer designationId;
    private Integer reportingManagerId;
    private Integer hrManagerId;
    private String policyGroup;
    private String empGradeId;
    private String businessGroupId;
    private String empBranch;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate offerDate;  // job_offer
    private LocalDate onBoardingDate;
    private LocalDate effectiveStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate resignationDate;
    private LocalDate offBoardingDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate retirementDate;
    private String currencyCode;
    private Long ctc;
    private String payrollCostCenterCode;
    private String salaryModeCode;
    private String pfNum;
    private Integer defaultShiftId;
    private Integer holidayListId;
    private Integer noticeDays;
    private Integer payrollCycle;
}

