package com.atomicnorth.hrm.tenant.service.dto.employement.employee_on_boarding;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class OnboardingActivitesDTO {

    private Integer activityId;

    private Integer onboardingId;

    @NotNull(message = "Activity name is required")
    @Size(max = 255, message = "Activity name cannot exceed 255 characters")
    private Integer activityCode;

    private String activityName;

    @NotNull(message = "Department is required")
    private Integer department;

    private String departmentName;

    @NotNull(message = "Assigned user is required")
    private Integer assignedUser;

    private String assignedUserName;

    @NotNull(message = "Begins on date is required")
    private Date beginsOn;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @NotNull(message = "Status is required")
    private String status;

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
}
