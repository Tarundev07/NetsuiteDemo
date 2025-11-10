package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class OfferTermMasterDTO {
    private Long id;

    @NotBlank(message = "Title is mandatory")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @NotBlank(message = "Type is mandatory")
    @Size(max = 200, message = "Type cannot exceed 200 characters")
    private String type;
    @NotBlank(message = "Value is mandatory")
    @Size(max = 256, message = "Value cannot exceed 256 characters")
    private String value;

    //@PastOrPresent(message = "Start Date cannot be in the future")
    private Date startDate;

    @FutureOrPresent(message = "End Date cannot be in the past")
    private Date endDate;
    private Date creationDate;

    private String createdBy;

    private String lastUpdatedBy;

    private Date lastUpdatedDate;
}
