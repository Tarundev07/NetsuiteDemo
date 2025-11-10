package com.atomicnorth.hrm.tenant.service.dto.employement.employee_job_applicant;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class JobApplicantDTO {

    private Integer id;

    @NotNull(message = "Applicant name cannot be empty or null")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Applicant name can only contain letters and spaces")
    @Size(min = 2, max = 100, message = "Applicant name must be between 2 and 100 characters")
    private String applicantName;

    private Integer jobOpeningId;

    private String jobOpeningName;

    @NotNull(message = "Email address cannot be null")
    @Email(message = "Invalid email format")
    private String emailAddress;

    @NotNull(message = "Designation cannot be null")
    private Integer designationId;

    private String designationName;

    private Integer departmentId;

    private String departmentName;

    private String contactCountryCode;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotNull(message = "Status cannot be null")
    private String status;

    @NotNull(message = "Country cannot be null")
    private Integer country;

    private String countryName;

    @NotNull(message = "Source cannot be null")
    private String source;

    private String sourceDetail;

    @Min(value = 1, message = "Applicant rating must be at least 1")
    @Max(value = 5, message = "Applicant rating cannot exceed 5")
    private Integer applicantRating;

    private String coverLetter;

    private String resumeLink;

    private String resumeAttachment;

    @NotNull(message = "Currency cannot be null")
    private String currency;

    @NotNull(message = "Lower range cannot be null")
    @Digits(integer = 10, fraction = 2, message = "Invalid lower range format")
    private BigDecimal lowerRange;

    @NotNull(message = "Upper range cannot be null")
    @Digits(integer = 10, fraction = 2, message = "Invalid upper range format")
    private BigDecimal upperRange;

    private String flag;

    @PastOrPresent(message = "Creation date cannot be in the future")
    private Date creationDate;

    private String createdBy;

    private String lastUpdatedBy;

    @PastOrPresent(message = "Last updated date cannot be in the future")
    private Date lastUpdatedDate;

    @JsonProperty("cAttribute1")
    private String cAttribute1;

    @JsonProperty("cAttribute2")
    private String cAttribute2;

    private Boolean isApplicantOffer;
}

