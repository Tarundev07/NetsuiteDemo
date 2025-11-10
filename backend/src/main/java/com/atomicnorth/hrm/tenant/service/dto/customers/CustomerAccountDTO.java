package com.atomicnorth.hrm.tenant.service.dto.customers;

import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class CustomerAccountDTO {
    private Integer accountId;
    private String accountCode;
    @NotNull(message = "Account name is required")
    @Size(max = 100)
    private String accountName;
    @NotNull(message = "Customer Name name is required")
    private Integer customerId;
    private String customerName;
    @NotNull(message = "description is required")
    private String description;
    @NotNull(message = "address is required")
    @Size(max=200)
    @NotNull(message = "address is required")
    private String address;
    private LocalDate endDate;
    @NotNull(message="Start date not null.")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
    @NotNull(message = "Payment Type is required")
    private String paymentType;
    @NotNull(message = "GST Number is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$", message = "Invalid GST number format")
    private String gstNumber;
    @Pattern(regexp = "\\d{6}", message = "PIN code must be 6 digits")
    private String pinCode;
    @NotNull(message = "Pan number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]", message = "Invalid PAN number format")
    private String panNumber;
    @NotNull(message = "Country name is required")
    private Integer countryId;
    private String countryName;
    @NotNull(message = "State name is required")
    private Integer stateId;
    private String stateName;
    @NotNull(message = "City name is required")
    private Integer cityId;
    private String cityName;
    private String isTaxable;
    private String isCreditEnable;
    private String isActive;
}

