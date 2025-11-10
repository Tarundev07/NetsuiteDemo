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
public class EmployeePersonalInfoDTO {
    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private String displayName;
    private String fatherName;
    private String motherName;
    private LocalDate dob;
    private String birthPlace;
    private String genderCode;
    private Integer nationality;
    private String maritalStatus;
    private LocalDate marriageDate;
    private String spouseName;
    private String uniqueIdentifier;
    private String motherTongue;
    private String bloodGroup;
    private String fitnessLevel;
    private String disability;
    private String disabilityPercentage;
    private String descriptionMedicalInfo;
    @NotBlank(message = "PanNumber_cannot_be_blank")
    private String panNumber;
    @NotBlank(message = "AadhaarNumber_cannot_be_blank")
    private String aadhaarNumber;
    private String dlNumber;
    private String passportNumber;
    private String placeOfIssuePassport;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfIssuePassport;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUpToPassport;
}

