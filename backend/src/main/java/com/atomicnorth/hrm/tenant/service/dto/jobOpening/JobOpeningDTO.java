package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class JobOpeningDTO {

    private Integer jobOpeningId;

    @NotBlank(message = "Job Title cannot be blank.")
    @Size(max = 500, message = "Job Title must not exceed 500 characters.")
    private String jobTitle;

    private Integer designationId;

    @NotBlank(message = "Status cannot be blank.")
    @Pattern(regexp = "Y|N", message = "Status must be either 'Y' or 'N'.")
    private String status;

    @NotNull(message = "Posted on is required.")
    @PastOrPresent(message = "Posted on date cannot be future.")
    private Date postedOn;
    @FutureOrPresent(message = "Closes on date cannot be past. ")
    @NotNull(message = "Closed on is required.")
    private Date closesOn;

    @NotBlank(message = "Employment Type cannot be blank.")
    @Size(max = 50, message = "Employment Type must not exceed 50 characters.")
    private String employmentType;

    private Integer departmentId;

    @NotBlank(message = "Location cannot be blank.")
    @Size(max = 200, message = "Location must not exceed 200 characters.")
    private String location;

    @NotBlank(message = "Publish On Website cannot be blank.")
    @Pattern(regexp = "Y|N", message = "Publish On Website must be either 'Y' or 'N'.")
    private String publishOnWebsite;

    @NotBlank(message = "Description cannot be blank.")
    private String description;

    @NotBlank(message = "Currency cannot be blank.")
    @Size(max = 10, message = "Currency must not exceed 10 characters.")
    private String currency;

    @NotBlank(message = "Salary Paid Per cannot be blank.")
    @Size(max = 50, message = "Salary Paid Per must not exceed 50 characters.")
    private String salaryPaidPer;

    @NotNull(message = "Lower Range cannot be null.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Lower Range must be greater than or equal to 0.")
    private Double lowerRange;

    @NotNull(message = "Upper Range cannot be null.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Upper Range must be greater than or equal to 0.")
    private Double upperRange;

    @NotBlank(message = "Publish Salary Range cannot be blank.")
    @Pattern(regexp = "Y|N", message = "Publish Salary Range must be either 'Y' or 'N'.")
    private String publishSalaryRange;

    private Date creationDate;
    private String createdBy;
    private String lastUpdatedBy;
    private Date lastUpdatedDate;
    private String recordInfo;
    private String departmentName;
    private String designationName;
}
