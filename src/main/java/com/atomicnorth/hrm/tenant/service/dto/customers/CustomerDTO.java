package com.atomicnorth.hrm.tenant.service.dto.customers;


import com.atomicnorth.hrm.tenant.domain.AbstractAuditingEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Date;

@Data
public class CustomerDTO extends AbstractAuditingEntity {
    private Integer customerId;
    @NotBlank(message = "Customer name is required .")
    private String customerName;

    @NotNull(message = "Sales representative Name is required")
    private Integer salesRepresentativeNumber;

    private String salesRepresentativeName;

    @NotBlank(message = "Customer description is required")
    private String customerDescription;

    @NotBlank(message = "Customer type is required")
    private String customerType;

    @NotNull(message = "Effective start date is required")
    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    @NotNull(message = "Country Name is required")
    private Integer countryId;

    private String countryName;

    @NotNull(message = "State Name is required")
    private Integer stateId;

    private String stateName;

    @NotNull(message = "City Name is required")
    private Integer cityId;

    private String cityName;

    @NotBlank(message = "Primary currency is required")
    private String primaryCurrency;

   // @NotBlank(message = "Customer website URL is required")
    private String customerWebsiteUrl;

    @NotBlank(message = "Active status is required")
    @Pattern(regexp = "Y|N", message = "Active status must be either 'Y' or 'N'")
    private String isActive;
}
