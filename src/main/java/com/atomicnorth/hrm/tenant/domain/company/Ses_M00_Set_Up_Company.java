package com.atomicnorth.hrm.tenant.domain.company;

import com.atomicnorth.hrm.tenant.domain.holiday.HolidaysCalendar;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ses_m00_set_up_company",
        uniqueConstraints = {
                @UniqueConstraint(name = "COMPANY_UNIQUE_NAME_ABBREVIATION", columnNames = {"COMPANY_NAME", "COMPANY_ABBREVIATION"})
        })
public class Ses_M00_Set_Up_Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMPANY_ID", nullable = false)
    private Integer companyId;

    @Column(name = "COMPANY_NAME", nullable = false, length = 255)
    private String companyName;

    @Column(name = "COMPANY_ABBREVIATION", nullable = false, length = 50)
    private String companyAbbreviation;

    @Column(name = "DEFAULT_CURRENCY", nullable = false)
    private Integer defaultCurrency;

    @Column(name = "COUNTRY", nullable = false)
    private Integer country;

    @Column(name = "DEFAULT_LETTER_HEAD")
    private Integer defaultLetterHead;

    @Column(name = "TAX_ID")
    private Integer taxId;

    @Column(name = "IS_GROUP")
    private Boolean isGroup;

    @Column(name = "DOMAIN", length = 255)
    private String domain;

    @Column(name = "DATE_OF_ESTABLISHMENT")
    @Temporal(TemporalType.DATE)
    private Date dateOfEstablishment;

    @Column(name = "PARENT_COMPANY")
    private Integer parentCompany;

    @Column(name = "DEFAULT_HOLIDAY_LIST")
    private Integer defaultHolidayList;

    @Column(name = "REGISTRATION_DETAILS", length = 255)
    private String registrationDetails;

    @Column(name = "GST_NO", length = 50)
    private String gstNo;

    @Column(name = "CHART_OF_ACCOUNT")
    private Integer chartOfAccount;

    @Column(name = "CHART_OF_ACCOUNT_TEMPLATE")
    private Integer chartOfAccountTemplate;

    @Column(name = "WRITE_OFF_ACCOUNT")
    private Integer writeOffAccount;

    @Column(name = "LOST_ACCOUNT")
    private Integer lostAccount;

    @Column(name = "DEFAULT_PAYMENT_DISCOUNT_ACCOUNT")
    private Integer defaultPaymentDiscountAccount;

    @Column(name = "PAYROLL_CYCLE", nullable = true)
    private Integer payrollCycle;

    @Column(name = "CREATED_ON")
    @Temporal(TemporalType.DATE)
    private Date createdOn;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "PAYMENT_TERM_TEMPLATE")
    private Integer paymentTermTemplate;

    @Column(name = "EXCHANGE_GAIN_LOSS")
    private Integer exchangeGainLoss;

    @Column(name = "UNRELEASED_GAIN_LOSS")
    private Integer unreleasedGainLoss;

    @Column(name = "ROUND_OFF_ACCOUNT")
    private Integer roundOffAccount;

    @Column(name = "ROUND_OFF_OPENING")
    private Integer roundOffOpening;

    @Column(name = "ROUND_OFF_COST_CENTER")
    private Integer roundOffCostCenter;

    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    @Transient
    private String HolidayName;

    @ManyToOne
    @JoinColumn(name = "DEFAULT_HOLIDAY_LIST", referencedColumnName = "HOLIDAY_CALENDAR_ID", insertable = false, updatable = false)
    private HolidaysCalendar holidaysCalendar;

    @ManyToOne
    @JoinColumn(name = "PARENT_COMPANY", referencedColumnName = "COMPANY_ID", insertable = false, updatable = false)
    private Ses_M00_Set_Up_Company parentCompanyEntity;
}