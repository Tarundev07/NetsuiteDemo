package com.atomicnorth.hrm.tenant.service.dto.project;

import lombok.Data;

import java.util.Date;

@Data
public class ProjectAllocationHistoryDTO {
    private Integer projectAllocationHistoryId;
    private Integer projectAllocationId;
    private Integer projectRfNum;
    private Integer employeeId;
    private String deputation;
    private Long unitPricePerHour;
    private Date startDate;
    private Date endDate;
    private String remark;
    private String isActive;
    private String isDeleted;
    private Integer cilentId;
}
