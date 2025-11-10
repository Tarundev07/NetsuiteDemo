package com.atomicnorth.hrm.tenant.service.dto;

import lombok.Data;

@Data
public class EmployeeProjectAllocationDTO {
    private String startDate;
    private String endDate;
    private Integer projectRfNum;
    private String projectName;
    private String projectStatus;
    private Double allocationPercentage;
    private String customerName;
}
