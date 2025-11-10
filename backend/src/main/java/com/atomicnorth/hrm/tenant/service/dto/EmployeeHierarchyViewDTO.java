package com.atomicnorth.hrm.tenant.service.dto;

import lombok.Data;

@Data
public class EmployeeHierarchyViewDTO {
    private Integer employeeId;
    private String employeeNumber;
    private String employeeName;
    private Integer divisionId;
    private String divisionName;
    private Integer departmentId;
    private String departmentName;
    private Integer reportingManagerId;
    private String reportingManagerName;
    private String reportingManagerNumber;
}
