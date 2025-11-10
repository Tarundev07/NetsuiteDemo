package com.atomicnorth.hrm.tenant.service.dto.employement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployee {
    private Integer jobApplicantId;
    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private String displayName;
    private String motherName;
    private String fatherName;
    private String spouseName;
    private String personalEmail;
    private String workEmail;
    private String primaryContactNumber;
    private String primaryContactCountryCode;
    private String secondaryContactNumber;
    private String secondaryContactCountryCode;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveStartDate;
    private String employeeType;
    private Long departmentId;
    private Integer designationId;
    private Integer reportingManagerId;
    private Integer hrManagerId;
    private String nationality;
    @NotBlank(message = "AadhaarNumber_cannot_be_blank")
    private String aadhaarNumber;
    @NotBlank(message = "PanNumber_cannot_be_blank")
    private String panNumber;
    private String primaryEmergencyContactName;
    private String primaryEmergencyContactRelation;
    private String primaryEmergencyContactNumber;
    private String primaryEmergencyContactCountryCode;
}
