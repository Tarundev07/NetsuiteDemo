package com.atomicnorth.hrm.tenant.service.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetUpCompanyDTOForResponse {

    private Integer companyId;
    private String companyName;
    private String companyAbbreviation;

    private Integer defaultCurrencyId;
    private String currency;

    private Integer countryId;
    private String countryName;

    private Integer defaultLetterHeadId;
    private String letterHeadName;

    private Integer taxId;
    private String tax;

    private Boolean isGroup;
    private String domain;
    private Date dateOfEstablishment;

    private Integer parentCompanyId;
    private String parentCompanyName;

    private Integer defaultHolidayList;
    private String holidayName;

    private String registrationDetails;
    private String gstNo;

    private Integer chartOfAccountId;
    private String chartOfAccountName;

    private Integer chartOfAccountTemplateId;
    private String chartOfAccountTemplateName;

    private Integer writeOffAccountId;
    private String writeOffAccountName;

    private Integer lostAccountId;
    private String lostAccountName;

    private Integer defaultPaymentDiscountAccountId;
    private String defaultPaymentDiscountAccountName;

    private Date createdOn;
    private String createdBy;

    private Integer paymentTermTemplateId;
    private String paymentTermTemplateName;

    private Integer exchangeGainLossId;
    private String exchangeGainLossName;


    private Integer unreleasedGainLossId;
    private String unreleasedGainLossName;


    private Integer roundOffAccountId;
    private String roundOffAccountName;


    private Integer roundOffOpeningId;
    private String roundOffOpeningName;


    private Integer roundOffCostCenterId;
    private String roundOffCostCenterName;

    private Integer payrollCycle;

}