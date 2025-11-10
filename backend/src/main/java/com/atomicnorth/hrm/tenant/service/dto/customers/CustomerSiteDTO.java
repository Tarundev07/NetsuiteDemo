package com.atomicnorth.hrm.tenant.service.dto.customers;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
public class CustomerSiteDTO {
    private Integer siteId;
    private Integer accountId;
    private String accountName;
    private String siteCode;
    @NotNull(message = "Site name is required")
    @Size(max = 100)
    private String siteName;
    @NotNull(message = "description name is required")
    private String description;
    @NotNull(message = "startDate name is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer countryId;
    private String countryName;
    private Integer stateId;
    private String stateName;
    private Integer cityId;
    private String cityName;
    @NotNull(message = "Address1 is required")
    private String address1;
    private String address2;
    private String address3;
    @Pattern(regexp = "\\d{6}", message = "PIN code must be 6 digits")
    private Integer pinCode;
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
    private String panNumber;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String billTo;
    private String shipTo;
    private String currency;
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$", message = "Invalid GST number format")
    private String gstNumber;
    private String isPrimary;
    private String isActive;
}


