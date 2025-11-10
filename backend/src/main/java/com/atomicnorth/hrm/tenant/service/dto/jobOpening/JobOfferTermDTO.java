package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import lombok.Data;

import javax.validation.constraints.*;
import java.util.Date;

@Data
public class JobOfferTermDTO {

    private Integer seqNo;

    private Integer offerTermId;
    @NotBlank(message = "Description cannot be empty")
    @Size(max = 240, message = "Description cannot exceed 240 characters")
    private String description;
    private String value;
    @NotBlank(message = "isActive cannot be blank.")
    @Pattern(regexp = "Y|N", message = "isActive must be either 'Y' or 'N'.")
    private String isActive;
    @NotNull(message = "Start date cannot be null")
    private Date startDate;
    @Future(message = "End date must be in the future")
    private Date endDate;

    private Date creationDate;

    private String createdBy;

    private String lastUpdatedBy;

    private Date lastUpdatedDate;


}
