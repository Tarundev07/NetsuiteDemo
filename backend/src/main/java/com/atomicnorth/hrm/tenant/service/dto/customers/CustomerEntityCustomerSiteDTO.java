package com.atomicnorth.hrm.tenant.service.dto.customers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntityCustomerSiteDTO implements Serializable {

    @NotNull(message = "Account ID is required")
    private Integer accountId;
    private Integer siteId;

    @Size(max = 100, message = "Site code should be at most 100 characters")
    private String siteCode;

    @NotNull(message = "Country ID is required")
    private Integer countryId;
    private String countryName;
    private Integer currencyId;

    @NotNull(message = "Name is required")
    @Size(max = 100, message = "Name should be at most 100 characters")
    private String name;

    @Size(max = 100, message = "Description should be at most 100 characters")
    private String description;

    @Size(max = 100, message = "GSTN should be at most 100 characters")
    @Pattern(regexp = "\\d{15}", message = "GSTN should be 15 digits")
    private String gstn;

    @NotNull(message = "Primary flag is required")
    @Pattern(regexp = "[YN]", message = "Primary flag should be 'Y' or 'N'")
    private String isPrimary;

    @NotNull(message = "Deleted flag is required")
    @Pattern(regexp = "[YN]", message = "Deleted flag should be 'Y' or 'N'")
    private String isDeleted;

    @NotNull(message = "Active flag is required")
    @Pattern(regexp = "[YN]", message = "Active flag should be 'Y' or 'N'")
    private String isActive;

    @Size(max = 200, message = "Attribute2 should be at most 200 characters")
    private String attribute2;

    @Size(max = 200, message = "Attribute3 should be at most 200 characters")
    private String attribute3;

    @Size(max = 50, message = "Created by should be at most 50 characters")
    private String createdBy;

    @NotNull(message = "Creation date is required")
    private Date creationDate;

    @Size(max = 50, message = "Last updated by should be at most 50 characters")
    private String lastUpdatedBy;

    @NotNull(message = "Last update date is required")
    private Date lastUpdateDate;

    // Add getters and setters for all fields

    // Add constructors as needed
}
