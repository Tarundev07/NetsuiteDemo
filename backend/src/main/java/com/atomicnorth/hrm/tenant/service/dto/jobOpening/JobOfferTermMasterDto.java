package com.atomicnorth.hrm.tenant.service.dto.jobOpening;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class JobOfferTermMasterDto {

    private Integer jobOfferTemplateId;
    @NotNull(message = "Offer term master ID cannot be null.")
    private Integer offerTermMasterId;

    private String title;

    private String type;
    @NotBlank(message = "Flag cannot be blank.")
    private String flag;
    @NotBlank(message = "Active status cannot be blank.")
    private String isActive;

    @NotBlank(message = "Value cannot be blank.")
    private String value;

}
