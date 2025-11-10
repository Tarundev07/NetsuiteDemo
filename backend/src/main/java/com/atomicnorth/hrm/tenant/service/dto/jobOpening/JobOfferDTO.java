package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class JobOfferDTO {

    private Integer jobOfferId;
    private String jobOfferName;
    @NotNull(message = "Job applicant ID is required.")
    private Integer jobApplicantId;
    private String jobApplicantName;

    @NotNull(message = "Designation ID is required.")
    private Integer designationId;
    private String designationName;
    private Integer termsConditionId;
    private String termAndConditionName;
    private Integer jobOfferTemplateId;
    private String jobOfferTemplateName;
    private Integer noticePeriod;
    private Integer departmentId;

    @NotBlank(message = "Status cannot be blank.")
    @Pattern(regexp = "Y|N", message = "Status must be either 'Y' or 'N'.")
    private String status;
    @NotNull(message = "Offer Date cannot be blank.")
    private LocalDate offerDate;

    @NotNull(message = "Start Date cannot be blank.")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Future(message = "End date must be in the future.")
    private Date endDate;
    private Date creationDate;
    private String createdBy;
    private String lastUpdatedBy;
    private Date lastUpdatedDate;
    private List<JobOfferTermDTO> jobOfferTerms;
    private boolean employeeCreated;

}
