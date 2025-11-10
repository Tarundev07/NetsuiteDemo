package com.atomicnorth.hrm.tenant.service.dto.employement.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {
    private Integer employeeId;
    //    @NotNull(message = "UserName_cannot_be_null")
    private String employeeNumber;
    //    @NotBlank(message = "Salutation_cannot_be_blank")
    private String salutation;
    //    @NotBlank(message = "FirstName_cannot_be_blank")
    private String firstName;
    private String middleName;
    private String lastName;
    private String displayName;
    //    @NotNull(message = "Dob_cannot_be_null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    //    @NotBlank(message = "GenderCode_cannot_be_blank")
    private String genderCode;
    //    @NotNull(message = "DepartmentId_cannot_be_null")
    private Integer divisionId;
    private Long departmentId;
    private Integer designationId;
    private Integer reportingManagerId;
    private Integer hrManagerId;
    private String isVerified;
    private Integer nationality;
    private String isActive;
    private String employeeType;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveEndDate;
    private String uniqueIdentifier;
    @NotBlank(message = "PanNumber_cannot_be_blank")
    private String panNumber;
    @NotBlank(message = "AadhaarNumber_cannot_be_blank")
    private String aadhaarNumber;
    private String passportNumber;
    private String dlNumber;
    //    @NotBlank(message = "BusinessGroupId_cannot_be_blank")
    private String businessGroupId;
    //    @NotBlank(message = "PolicyGroup_cannot_be_blank")
    private String policyGroup;
    private String motherTongue;
    //    @NotBlank(message = "Marital_Status_cannot_be_blank")
    private String maritalStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate marriageDate;
    private String spouseName;
    private String fatherName;
    private String motherName;
    private String personalEmail;
    private String workEmail;
    private String primaryContactNumber;
    private String primaryContactCountryCode;
    private String secondaryContactNumber;
    private String secondaryContactCountryCode;
    private String primaryEmergencyContactName;
    private String primaryEmergencyContactRelation;
    private String primaryEmergencyContactNumber;
    private String primaryEmergencyContactCountryCode;
    private String secondaryEmergencyContactName;
    private String secondaryEmergencyContactRelation;
    private String secondaryEmergencyContactNumber;
    private String secondaryEmergencyContactCountryCode;
    private String websiteUrl;
    private String linkedinUrl;
    private String birthPlace;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate onBoardingDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate offBoardingDate;
    private String baseLocation;
    private String bloodGroup;
    private String fitnessLevel;
    private String disability;
    private String disabilityPercentage;
    private String descriptionMedicalInfo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate retirementDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate confirmationDate;
    private Integer noticeDays;
    //    @NotNull(message = "PayrollCostCenterCode_cannot_be_null")
    private String payrollCostCenterCode;  //lookup
    private Long ctc;
    //    @NotNull(message = "CurrencyCode_cannot_be_null")
    private String currencyCode; //lookup
    //    @NotNull(message = "SalaryModeCode_cannot_be_null")
    private String salaryModeCode; // lookup
    private String pfNum;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfIssuePassport;
    private String placeOfIssuePassport;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validUpToPassport;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate resignationDate;
    private String empGradeId;
    private String empBranch;
    //    @NotNull(message = "HolidayListId_cannot_be_null")
    private Integer holidayListId;
    //    @NotNull(message = "DefaultShiftId_cannot_be_null")
    private Integer defaultShiftId;
    //    @NotNull(message = "JobApplicantId_cannot_be_null")
    private Integer jobApplicantId;//job_Offer
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate offerDate;  // job_offer
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private LocalDate creationDate;
    private String createdBy;
    private String lastUpdatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastUpdateDate;


    private String divisionName;
    private String designationName;
    private String departmentName;
    private String jobApplicantName;
    private String empGradeName;
    private String payrollCostCenterName;
    private String salaryModeName;
    private String currencyName;


    private String holidayListName;
    private String fullName;
    private Integer payrollCycle;

}
