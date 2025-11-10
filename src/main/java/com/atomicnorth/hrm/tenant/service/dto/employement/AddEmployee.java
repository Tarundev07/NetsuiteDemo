package com.atomicnorth.hrm.tenant.service.dto.employement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddEmployee {

    private String firstName;
    private String middleName;
    private String lastName;
    private String displayName;
    private String personalEmail;
    private String workEmail;
    private Long primaryContactNumber;
    private String primaryContactCountryCode;
    private String employeeType;
    private Integer divisionId;
    private Long departmentId;
    private Integer designationId;
    private Integer reportingManagerId;
    private Integer hrManagerId;
}
