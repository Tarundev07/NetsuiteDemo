package com.atomicnorth.hrm.tenant.service.dto;


import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class LeaveRequestMappingDTO {
    private Long id;
    private Long leaveRfNum;
    private String requestNumber;
    private Long leaveCode;
    private String leaveName;
    private Integer empId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;
    private Double noOfDays;
    private String fullDayFlag;
    private Instant lastUpdatedDate;
}
