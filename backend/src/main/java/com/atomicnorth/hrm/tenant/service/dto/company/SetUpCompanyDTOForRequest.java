package com.atomicnorth.hrm.tenant.service.dto.company;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetUpCompanyDTOForRequest {

    private Integer companyId;

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must be less than 255 characters")
    private String companyName;

    @NotBlank(message = "Company abbreviation is required")
    @Size(max = 50, message = "Company abbreviation must be less than 50 characters")
    private String companyAbbreviation;

    @NotNull(message = "Default currency is required")
    private Integer defaultCurrency;

    @NotNull(message = "Country is required")
    private Integer country;

    private Integer defaultLetterHead;
    private Integer taxId;
    private Boolean isGroup;

    @Size(max = 255, message = "Domain must be less than 255 characters")
    private String domain;

    private Date dateOfEstablishment;
    private Integer parentCompany;
    private Integer defaultHolidayList;

    @Size(max = 255, message = "Registration details must be less than 255 characters")
    private String registrationDetails;

    @Size(max = 50, message = "GST number must be less than 50 characters")
    private String gstNo;

    private Integer chartOfAccount;
    private Integer chartOfAccountTemplate;
    private Integer writeOffAccount;
    private Integer lostAccount;
    private Integer defaultPaymentDiscountAccount;
    private Integer paymentTermTemplate;
    private Integer exchangeGainLoss;
    private Integer unreleasedGainLoss;
    private Integer roundOffAccount;
    private Integer roundOffOpening;
    private Integer roundOffCostCenter;
    private Integer payrollCycle;

}
