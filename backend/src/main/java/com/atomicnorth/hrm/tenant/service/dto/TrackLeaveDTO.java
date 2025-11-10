package com.atomicnorth.hrm.tenant.service.dto;

import lombok.Data;

import java.time.Instant;


@Data
public class TrackLeaveDTO {

    private Long leaveRfNum;
    private String requestNumber;
    private Integer empId;
    private String empName;
    private String leaveSummary;
    private String status;
    private Double totalDays;
    private String createdBy;
    private String lastUpdatedBy;
    private Instant createdDate;
    private Instant lastUpdatedDate;
}
