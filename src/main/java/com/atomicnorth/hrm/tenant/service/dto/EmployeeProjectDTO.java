package com.atomicnorth.hrm.tenant.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProjectDTO {
    private Integer empId;
    private String employeeNumber;
    private String empName;
    private String employmentType;
    private Integer rmId;
    private String rmName;
    private Set<String> activeProjects = new TreeSet<>();
}

