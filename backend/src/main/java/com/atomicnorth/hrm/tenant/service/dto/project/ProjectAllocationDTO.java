package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectAllocationDTO {
    private Integer projectAllocationId;
    private Integer projectRfNum;
    private String projectId;
    private Integer employeeId;
    private String employeeName;
    private String employeeNumber;
    private String deputation;
    private Long unitPricePerHour;
    private Date startDate;
    private Date endDate;
    private String remark;
    private String isActive;
    private String isDeleted;
    private Integer roleId;
    private Double allocationPercentage;
    private String clientTimesheet;
}
