package com.atomicnorth.hrm.tenant.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "employee_hierarchy_v")
public class EmployeeHierarchyView {

    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "EMPLOYEE_NUMBER")
    private String employeeNumber;

    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    @Column(name = "DIVISION_ID")
    private Integer divisionId;

    @Column(name = "DIVISION_NAME")
    private String divisionName;

    @Column(name = "DEPARTMENT_ID")
    private Integer departmentId;

    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    @Column(name = "DESIGNATION_ID")
    private Integer designationId;

    @Column(name = "DESIGNATION_NAME")
    private String designationName;

    @Column(name = "REPORTING_MANAGER_ID")
    private Integer reportingManagerId;

    @Column(name = "REPORTING_MANAGER_NAME")
    private String reportingManagerName;

    @Column(name = "REPORTING_MANAGER_NUMBER")
    private String reportingManagerNumber;

    @Column(name = "EMPLOYEE_GROUPS")
    private String employeeGroup;
}
