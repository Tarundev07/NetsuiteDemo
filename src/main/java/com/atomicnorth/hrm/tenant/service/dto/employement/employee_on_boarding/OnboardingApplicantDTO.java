package com.atomicnorth.hrm.tenant.service.dto.employement.employee_on_boarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class OnboardingApplicantDTO {

    private Integer onboardingId;

    @NotNull(message = "Job offer is required")
    private Integer jobOffer;

    private String jobOfferName;

    @NotNull(message = "Job Applicant is required")
    private Integer jobApplicantId;

    private String applicantName;

    @NotNull(message = "Employee onboarding template is required")
    private Integer employeeOnboardingTemplate;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    @NotNull(message = "Department is required")
    private Integer department;

    private String departmentName;

    @NotNull(message = "Onboarding begins on date is required")
    private LocalDate onboardingBeginsOn;

    @NotNull(message = "Designation is required")
    private Integer designation;

    private String designationName;

    private Integer holiday;

    private String holidayName;

    private String fullName;

    @NotNull(message = "Notify user by email field is required")
    private Character notifyUserByEmail;

    @NotBlank(message = "Operation source is required")
    @Size(max = 10, message = "Operation source cannot exceed 10 characters")
    private String operationSource;

    @JsonProperty("cAttribute1")
    private String cAttribute1;

    @JsonProperty("cAttribute2")
    private String cAttribute2;

    @JsonProperty("cAttribute3")
    private String cAttribute3;

    @JsonProperty("cAttribute4")
    private String cAttribute4;

    @JsonProperty("cAttribute5")
    private String cAttribute5;

    @JsonProperty("cAttribute6")
    private String cAttribute6;

    @JsonProperty("cAttribute7")
    private String cAttribute7;

    @JsonProperty("cAttribute8")
    private String cAttribute8;

    @JsonProperty("cAttribute9")
    private String cAttribute9;

    @JsonProperty("cAttribute10")
    private String cAttribute10;

    @JsonProperty("cAttribute11")
    private String cAttribute11;

    @JsonProperty("cAttribute12")
    private String cAttribute12;

    @JsonProperty("cAttribute13")
    private String cAttribute13;

    @JsonProperty("cAttribute14")
    private String cAttribute14;

    @JsonProperty("cAttribute15")
    private String cAttribute15;

    @JsonProperty("nAttribute16")
    private Integer nAttribute16;

    @JsonProperty("nAttribute17")
    private Integer nAttribute17;

    @JsonProperty("nAttribute18")
    private Integer nAttribute18;

    @JsonProperty("nAttribute19")
    private Integer nAttribute19;

    @JsonProperty("nAttribute20")
    private Integer nAttribute20;

    @JsonProperty("nAttribute21")
    private Integer nAttribute21;

    @JsonProperty("nAttribute22")
    private Integer nAttribute22;

    @JsonProperty("nAttribute23")
    private Integer nAttribute23;

    @JsonProperty("nAttribute24")
    private Integer nAttribute24;

    @JsonProperty("nAttribute25")
    private Integer nAttribute25;

    @JsonProperty("dAttribute26")
    private Date dAttribute26;

    @JsonProperty("dAttribute27")
    private Date dAttribute27;

    @JsonProperty("dAttribute28")
    private Date dAttribute28;

    @JsonProperty("dAttribute29")
    private Date dAttribute29;

    @JsonProperty("dAttribute30")
    private Date dAttribute30;

    @JsonProperty("jAttribute31")
    private String jAttribute31;

    @JsonProperty("jAttribute32")
    private String jAttribute32;

    @NotEmpty(message = "Onboarding activities list cannot be empty")
    private List<OnboardingActivitesDTO> onboardingActivities;
}
