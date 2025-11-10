package com.atomicnorth.hrm.tenant.service.dto.employeeExit;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TrackSeparationDTO {

    private Integer employeeId;
    private String employeeName;
    private String exitRequestNumber;
    private Long departmentId;
    private String departmentName;
    private LocalDate joiningDate;
    private LocalDate exitDate;
    private Integer noticePeriod;
    private LocalDate lastWorkingDate;
    private String tenure;

    private String approvalPerson;
    private String approvalStatus;
    private LocalDate approvalDate;
    private String approvalRemarks;

    private String assetPerson;
    private String assetStatus;
    private String assetRemarks;
    private LocalDate assetDate;

    private String financePerson;
    private String financeStatus;
    private String financeRemarks;
    private LocalDate financeDate;

    private String adminPerson;
    private String adminStatus;
    private String adminRemarks;
    private LocalDate adminDate;

    private String ktPerson;
    private String ktStatus;
    private String ktRemarks;
    private LocalDate ktDate;

    private String interviewPerson;
    private String interviewStatus;
    private String interviewRemarks;
    private LocalDate interviewDate;
}
