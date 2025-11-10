package com.atomicnorth.hrm.tenant.service.dto;


import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class EmployeeDTO implements Serializable {

    private Integer employeeId;

    private Integer username;

    @NotNull(message = "IsVerified cannot be null")
    private String isVerified;

    @NotNull(message = "FirstName cannot be null")
    private String firstName;

    @NotNull(message = "MiddleName cannot be null")
    private String middleName;

    @NotNull(message = "LastName cannot be null")
    private String lastName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String email;
    private String mobileNo;
    private String fullName;
    private String employeeNumber;
    public EmployeeDTO(Integer employeeId, String firstName, String middleName, String lastName, String email) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.fullName = (firstName != null ? firstName : "")
                + (middleName != null ? " " + middleName : "")
                + (lastName != null ? " " + lastName : "");
    }

    public EmployeeDTO(Integer employeeId, String fullName, String email,String employeeNumber) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.employeeNumber= employeeNumber;
    }

    public EmployeeDTO() {

    }

    public EmployeeDTO(Integer employeeId, String fullName) {
        this.employeeId = employeeId;
        this.fullName = fullName;
    }
    private String isActive;
}
